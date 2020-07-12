import {Component, OnInit, ViewChild} from '@angular/core';
import {Exam} from '../../../models/exam';
import {ExamService} from '../../../_services/exam.service';
import {ExamResult} from '../../../models/exam-result';
import {ActivatedRoute, Router} from '@angular/router';
import _ from 'lodash';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexPlotOptions,
  ApexStroke, ApexTitleSubtitle,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis, ChartComponent
} from 'ng-apexcharts';
import {ColorsService} from '../../../_services/colors.service';

export interface ChartOptions {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xAxis: ApexXAxis;
  stroke: ApexStroke;
  dataLabels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  yAxis: ApexYAxis;
  colors: string[];
  subtitle: ApexTitleSubtitle;
}

@Component({
  selector: 'app-user-test',
  templateUrl: './user-test.component.html',
  styleUrls: ['./user-test.component.scss']
})
export class UserTestComponent implements OnInit {
  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: Partial<ChartOptions>;
  examResults: ExamResult[] = [];
  examId: number;
  sortBy = false;
  dataSort: ExamResult[] = [];

  constructor(private examService: ExamService, private route: ActivatedRoute, private colorsService: ColorsService, private router: Router) {
  }

  ngOnInit(): void {
    this.getExamList();
  }

  getExamList() {
    this.examId = Number(this.route.snapshot.paramMap.get('id'));
    this.examService.getExamResultListByExamId(this.examId).subscribe(data => {
      this.examResults = data;
      this.dataSort = _.shuffle(data);
      this.dataSort = this.dataSort.slice(0, 10);
      console.log(this.dataSort);
      this.chartOptions = {
        series: [
          {
            name: 'Result',
            data:
              this.dataSort.map(value => value.totalPoint || NaN)
          }
        ],
        chart: {
          type: 'bar',
          height: 380
        },
        plotOptions: {
          bar: {
            barHeight: '100%',
            distributed: true,
            horizontal: true,
            dataLabels: {
              position: 'bottom'
            }
          }
        },
        colors: this.dataSort.map(value => this.colorsService.getRandomColor()),
        dataLabels: {
          enabled: true,
          textAnchor: 'start',
          style: {
            colors: ['#fff']
          },
          formatter(val, opt) {
            return opt.w.globals.labels[opt.dataPointIndex] + ': ' + val + ' point';
          },
          offsetX: 0,
          dropShadow: {
            enabled: true
          }
        },
        stroke: {
          width: 1,
          colors: ['#fff']
        },
        xAxis: {
          categories: this.dataSort.map(value => value.user.profile.lastName + ' ' + value.user.profile.firstName)
        },
        yAxis: {
          labels: {
            show: false
          }
        },

        subtitle: {
          text: 'Category Names as DataLabels inside bars',
          align: 'center'
        },

      };
    });
  }

  trackById(item, index) {
    return item.id === index;
  }

  sortPoint() {
    this.sortBy = !this.sortBy;
    if (this.sortBy) {
      this.examResults = _.orderBy(this.examResults, 'totalPoint', 'desc');
    } else {
      this.examResults = _.orderBy(this.examResults, 'totalPoint', 'asc');

    }
  }

  goUserExamDetail(username: string, userProfile: string) {
    this.router.navigate([`admin/tests/${this.examId}/users`, username]);
  }
}
