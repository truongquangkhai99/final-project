package com.thanhtam.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanhtam.backend.dto.*;
import com.thanhtam.backend.entity.*;
import com.thanhtam.backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api")
public class ExamController {
    Logger logger = LoggerFactory.getLogger(ExamController.class);

    private ExamService examService;
    private QuestionService questionService;
    private UserService userService;
    private IntakeService intakeService;
    private PartService partService;
    private ExamUserService examUserService;
    private ObjectMapper mapper;

    @Autowired
    public ExamController(ExamService examService, QuestionService questionService, UserService userService, IntakeService intakeService, PartService partService, ExamUserService examUserService, ObjectMapper mapper) {
        this.examService = examService;
        this.questionService = questionService;
        this.userService = userService;
        this.intakeService = intakeService;
        this.partService = partService;
        this.examUserService = examUserService;
        this.mapper = mapper;
    }

    @GetMapping(value = "/exams")
    public ResponseEntity<List<Exam>> getAll() {
        List<Exam> exams = examService.getAll();
        if (exams.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(exams, HttpStatus.OK);
    }

    @GetMapping(value = "/exams/list-all-by-user")
    public ResponseEntity<List<ExamUser>> getAllByUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.error(username);

        List<ExamUser> examUserList = examUserService.getExamListByUsername(username);
        return new ResponseEntity(examUserList, HttpStatus.OK);

    }

    @GetMapping(value = "/exams/exam-user/{examId}")
    public ResponseEntity<ExamUser> getExamUserById(@PathVariable Long examId) {
        String username = userService.getUserName();
        Optional<ExamUser> examUser = Optional.ofNullable(examUserService.findByExamAndUser(examId, username));
        if (!examUser.isPresent()) {
            return new ResponseEntity("Không tìm thấy exam user này", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(examUser.get());
    }

    @GetMapping(value = "/exams/{examId}/questions")
    public ResponseEntity<ExamQuestionList> getAllQuestions(@PathVariable Long examId) throws IOException {
        String username = userService.getUserName();
        ExamQuestionList examQuestionList = new ExamQuestionList();
        Optional<Exam> exam = examService.getExamById(examId);
        if (!exam.isPresent()) {
            return new ResponseEntity("Không tìm thấy exam này", HttpStatus.NOT_FOUND);
        }
        ExamUser examUser = examUserService.findByExamAndUser(examId, username);
        if (examUser.getIsStarted() == true) {
//            Get answersheet
            //            Convert question data json to array object

            List<AnswerSheet> choiceUsers = convertAnswerJsonToObject(examUser);

            List<Question> questions1 = new ArrayList<>();
            choiceUsers.forEach(answerSheet1 -> {
                Question question = questionService.getQuestionById(answerSheet1.getQuestionId()).get();
                question.setChoices(answerSheet1.getChoices());
                question.setPoint(answerSheet1.getPoint());
                questions1.add(question);
            });

            examQuestionList.setQuestions(questions1);
            examQuestionList.setExam(exam.get());
            logger.error("case 1");
        } else if (exam.get().isShuffle() == true) {
            ObjectMapper mapper = new ObjectMapper();
            String answerSheet = exam.get().getQuestionData();
            List<ExamQuestionPoint> examQuestionPoints = mapper.readValue(answerSheet, new TypeReference<List<ExamQuestionPoint>>() {
            });
            Collections.shuffle(examQuestionPoints);
//            save to answer sheet
            List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);
            List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);
            //            Convert answer sheet to json
            String answerSheetConvertToJson = mapper.writeValueAsString(answerSheets);
            examUser.setAnswerSheet(answerSheetConvertToJson);
            examQuestionList.setExam(exam.get());
            examUser.setIsStarted(true);
            examUserService.update(examUser);

            List<Question> questions1 = new ArrayList<>();
            answerSheets.forEach(answerSheet1 -> {
                Question question = questionService.getQuestionById(answerSheet1.getQuestionId()).get();
                question.setChoices(answerSheet1.getChoices());
                question.setPoint(answerSheet1.getPoint());
                questions1.add(question);
            });
            examQuestionList.setQuestions(questions1);
            examUser.setTimeStart(new Date());
            logger.error("case 2");

        } else {
            //            save to answer sheet
//            convert question json to object list
            ObjectMapper mapper = new ObjectMapper();
            String answerSheet = exam.get().getQuestionData();
            List<ExamQuestionPoint> examQuestionPoints = mapper.readValue(answerSheet, new TypeReference<List<ExamQuestionPoint>>() {
            });

            List<Question> questions = questionService.getQuestionPointList(examQuestionPoints);
            List<AnswerSheet> answerSheets = questionService.convertFromQuestionList(questions);
//            Convert answer sheet to json
            String answerSheetConvertToJson = mapper.writeValueAsString(answerSheets);
            examUser.setAnswerSheet(answerSheetConvertToJson);
            examUser.setIsStarted(true);
            examUser.setTimeStart(new Date());
            examUserService.update(examUser);
            List<Question> questions1 = new ArrayList<>();
            answerSheets.forEach(answerSheet1 -> {
                Question question = questionService.getQuestionById(answerSheet1.getQuestionId()).get();
                question.setChoices(answerSheet1.getChoices());
                question.setPoint(answerSheet1.getPoint());
                questions1.add(question);
            });
            examQuestionList.setQuestions(questions1);
            examQuestionList.setExam(exam.get());
            logger.error("case 3");

        }
        return new ResponseEntity(examQuestionList, HttpStatus.OK);

    }

    @PostMapping(value = "/exams")
    public ResponseEntity<?> createExam(@Valid @RequestBody Exam exam, @RequestParam Long intakeId, @RequestParam Long partId) {
        try {
            Optional<Intake> intake = intakeService.findById(intakeId);
            if (intake.isPresent()) {
                exam.setIntake(intake.get());
            }
            Optional<Part> part = partService.findPartById(partId);
            if (part.isPresent()) {
                exam.setPart(part.get());
            }

            this.examService.saveExam(exam);
            List<User> users = userService.findAllByIntakeId(intakeId);
            examUserService.create(exam, users);

//            Convert question data json to array object
            ObjectMapper mapper = new ObjectMapper();
            String questionJson = exam.getQuestionData();
            List<ExamQuestionPoint> examQuestionPoints = mapper.readValue(questionJson, new TypeReference<List<ExamQuestionPoint>>() {
            });
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

//    @PatchMapping(value="/exams/{examId}/join/users")
//    public List<User> addUserListToExam(@Valid @RequestBody List<User> users, @PathVariable Long examId){
//        Exam exam = this.examService.getExamById(examId).get();
//        exam.setId(examId);
//        exam.setUsers(users);
//        this.examService.
//
//    }

    @GetMapping(value = "/exams/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable("id") Long id) {
        Optional<Exam> exam = examService.getExamById(id);
        if (!exam.isPresent()) {
            return new ResponseEntity<>(exam.get(),
                    HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(exam.get(), HttpStatus.OK);
    }

    @PutMapping(value = "/exams/{examId}/questions-by-user")
    public void saveUserExamAnswer(@RequestBody List<AnswerSheet> answerSheets, @PathVariable Long examId, @RequestParam boolean isFinish) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<ExamUser> examUser = Optional.ofNullable(examUserService.findByExamAndUser(examId, username));
        if (!examUser.isPresent()) {
            throw new EntityNotFoundException("Not found this exam");
        } else {
            if (examUser.get().getIsFinished()) {

                throw new ExceptionInInitializerError("This exam was end");
            }
            ObjectMapper mapper = new ObjectMapper();
            String answerSheetConvertToJson = mapper.writeValueAsString(answerSheets);
            examUser.get().setAnswerSheet(answerSheetConvertToJson);
            examUser.get().setIsFinished(isFinish);
            if (isFinish == true) {
                examUser.get().setTimeFinish(new Date());
            }
            examUserService.update(examUser.get());
        }

    }

    @GetMapping(value = "/exams/{examId}/result")
    public ResponseEntity getResultExam(@PathVariable Long examId) throws IOException {
        ExamResult examResult = new ExamResult();
        String username = userService.getUserName();
        Optional<Exam> exam = examService.getExamById(examId);
        if (!exam.isPresent()) {
            return new ResponseEntity("Không tìm thấy exam", HttpStatus.NOT_FOUND);
        }
//        Set exam for examResult
        examResult.setExam(exam.get());

//        Set list question user's choice for examResult
        List<ExamQuestionPoint> examQuestionPoints = convertQuestionJsonToObject(exam);
        ExamUser examUser = examUserService.findByExamAndUser(examId, username);
        List<AnswerSheet> userChoices = convertAnswerJsonToObject(examUser);
        List<ChoiceList> choiceLists = examService.getChoiceList(userChoices, examQuestionPoints);
        examResult.setChoiceList(choiceLists);
        logger.error(examResult.toString());
        return new ResponseEntity(examResult, HttpStatus.OK);
    }

    public List<AnswerSheet> convertAnswerJsonToObject(ExamUser examUser) throws IOException {

//        ObjectMapper mapper = new ObjectMapper();
        String answerSheet = examUser.getAnswerSheet();
        List<AnswerSheet> choiceUsers = mapper.readValue(answerSheet, new TypeReference<List<AnswerSheet>>() {
        });
        return choiceUsers;
    }

    public List<ExamQuestionPoint> convertQuestionJsonToObject(Optional<Exam> exam) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
        String answerSheet = exam.get().getQuestionData();
        List<ExamQuestionPoint> examQuestionPoints = mapper.readValue(answerSheet, new TypeReference<List<ExamQuestionPoint>>() {
        });
        return examQuestionPoints;
    }

}


