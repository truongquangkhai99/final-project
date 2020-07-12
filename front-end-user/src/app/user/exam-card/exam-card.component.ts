import {Component, Input, OnChanges, OnInit} from '@angular/core';
import * as moment from 'moment';
import {ExamUser} from '../../models/exam-user';

@Component({
  selector: 'app-exam-card',
  templateUrl: './exam-card.component.html',
  styleUrls: ['./exam-card.component.scss']
})
export class ExamCardComponent implements OnInit, OnChanges {
  @Input() examUser: any;
  status: number;
  now = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');

  icon: string;

  constructor() {
  }

  ngOnChanges(changes: import('@angular/core').SimpleChanges): void {
    const beginDate = moment(this.examUser.exam.beginExam).format('YYYY-MM-DD HH:mm:ss');
    const finishDate = moment(this.examUser.exam.finishExam).format('YYYY-MM-DD HH:mm:ss');
    if (moment(finishDate).isBefore(this.now) && this.examUser.isStarted === false) {
      this.status = -2;
    } else if (this.examUser.isFinished === true) {
      this.status = -1;
    } else if (moment(beginDate).isAfter(this.now) || this.examUser.isStarted === false) {
      this.status = 0;
    } else {
      this.status = 1;
    }

  }

  ngOnInit(): void {
    console.log(this.examUser);
  }

}
