import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { Router } from '@angular/router';

import { LoginModalService, AccountService, Account } from 'app/core';
import { HomeService } from './home.service';
import { IRegionStat } from './home.model';
import { AppointmentService } from 'app/entities/appointment';
import { IAppointment } from 'app/shared/model/appointment.model';
import { Moment } from 'moment';
import * as moment from 'moment';

var echarts = require('echarts');
declare var jQuery: any;

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['home.scss']
})
export class HomeComponent implements OnInit {
  account: Account;
  modalRef: NgbModalRef;
  appointments: IAppointment[];
  allRegions: any[];
  currentRegion = '1号泊位';
  allDates: any[] = ['今日', '昨日', '最近七天'];
  currentDate = '今日';

  public resourceUrl = SERVER_API_URL + 'api/admin-dashboard';

  constructor(
    private accountService: AccountService,
    private loginModalService: LoginModalService,
    protected appointmentService: AppointmentService,
    private eventManager: JhiEventManager,
    private homeService: HomeService,
    private router: Router,
    private http: HttpClient
  ) {}

  loadLatestAppointments() {
    const filterParams = {
      page: 0,
      size: 15,
      sort: ['updateTime,desc']
    };

    this.appointmentService.query(filterParams).subscribe(
      (res: HttpResponse<IAppointment[]>) => {
        // console.log(res.body);
        this.appointments = res.body;
      },
      (res: HttpErrorResponse) => {
        console.log(res.message);
      }
    );
  }

  initCharts() {
    // NOW TIMER
    // -----------------------------------
    (function($) {
      'use strict';

      function initNowTimer() {
        $('[data-now]').each(function() {
          var element = $(this),
            format = element.data('format');

          function updateTime() {
            var dt = moment(new Date()).format(format);
            element.text(dt);
          }

          updateTime();
          setInterval(updateTime, 1000);
        });
      }

      initNowTimer();
    })(jQuery);

    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('company-load-stat'));
    var appointmentCountChart = echarts.init(document.getElementById('appointment-count-stat'));

    // 指定图表的配置项和数据
    var option = {
      title: {
        text: '总体发运量',
        subtext: '各公司发运量统计',
        x: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c}吨 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'right',
        data: ['直接访问', '邮件营销', '联盟广告', '视频广告', '搜索引擎']
      },
      series: [
        {
          name: '公司',
          type: 'pie',
          radius: '60%',
          center: ['50%', '60%'],
          data: [
            { value: 335, name: '直接访问' },
            { value: 310, name: '邮件营销' },
            { value: 234, name: '联盟广告' },
            { value: 135, name: '视频广告' },
            { value: 1548, name: '搜索引擎' }
          ],
          itemStyle: {
            emphasis: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };

    var countOption = {
      title: {
        text: '发运量统计',
        subtext: '各公司发运车次统计',
        x: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: function(params, ticket, callback) {
          return (
            params.name +
            '<br/>车次: ' +
            params.data['data']['count'] +
            '辆, ' +
            '发运量: ' +
            params.data['data']['weight'].toFixed(2) +
            '吨, 占比: ' +
            params.percent +
            '%'
          );
        }
        // formatter: "{a} <br/>{b} : {c}辆 ({d}%)"
      },
      legend: {
        orient: 'vertical',
        left: 'right',
        data: []
      },
      series: [
        {
          name: '公司',
          type: 'pie',
          radius: '60%',
          center: ['50%', '60%'],
          data: [],
          itemStyle: {
            emphasis: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };

    this.http.get(`${this.resourceUrl}/weight-stats?currentRegion=${this.currentRegion}&date=${this.currentDate}`).subscribe(
      (res: HttpResponse<any>) => {
        countOption.title.text = res['region'] + this.currentDate + '发运量统计';
        option.title.subtext += ' - ' + res['date'];
        countOption.title.subtext = '发运总车次：' + res['totalCount'] + '，总量：' + res['totalWeight'].toFixed(2) + '吨';
        // countOption.title.subtext += ' - ' + res['date'];
        option.series[0].data = [];
        countOption.series[0].data = [];
        for (let i = 0; i < res['data'].length; i++) {
          option.series[0].data.push({ value: res['data'][i]['weight'], name: res['data'][i]['name'] });
          countOption.series[0].data.push({
            value: res['data'][i]['weight'],
            name: res['data'][i]['name'],
            data: res['data'][i]
          });
        }
        // option.legend['data'] = res['companies'];
        option.legend['data'] = [];
        countOption.legend['data'] = [];
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(countOption);
        // myChart.setOption(option);
        // countChart.setOption(countOption);
      },
      (res: HttpErrorResponse) => {
        console.log(res.message);
      }
    );

    // 预约情况统计
    var appointmentOption = {
      title: {
        text: '各区域预约取号统计',
        x: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        top: '30px',
        data: ['剩余名额', '预约成功', '进厂', '排队中', '离场', '取消', '过期'],
        selected: {
          排队中: true,
          离场: false,
          取消: false,
          过期: false
        }
        // data: []
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: []
        // data: ['周一','周二','周三','周四','周五','周六','周日']
      },
      series: [
        // {
        //   name: '直接访问',
        //   type: 'bar',
        //   stack: '总量',
        //   label: {
        //     normal: {
        //       show: true,
        //       position: 'insideRight'
        //     }
        //   },
        //   data: [320, 302, 301, 334, 390, 330, 320]
        // },
      ]
    };

    this.http.get(`${this.resourceUrl}/appointment-stats?currentRegion=${this.currentRegion}&date=${this.currentDate}`).subscribe(
      (res: HttpResponse<any>) => {
        appointmentOption.yAxis.data = res['regions'];
        let status = ['available', 'wait', 'start', 'enter', 'leave', 'cancel', 'expired'];
        let statusName = {
          available: '剩余名额',
          wait: '排队中',
          start: '预约成功',
          enter: '进厂',
          leave: '离场',
          cancel: '取消',
          expired: '过期'
        };
        let stack = {
          available: '额度',
          wait: '总量',
          start: '额度',
          enter: '额度',
          leave: '总量',
          cancel: '总量',
          expired: '总量'
        };
        for (let x = 0; x < status.length; x++) {
          let item = {
            name: statusName[status[x]],
            type: 'bar',
            stack: stack[status[x]],
            label: {
              normal: {
                show: stack[status[x]] == '额度',
                position: 'inside'
              }
            },
            data: []
          };
          for (let i = 0; i < res['regions'].length; i++) {
            item.data.push(res['data'][i][status[x]]);
          }
          appointmentOption.series.push(item);
        }

        appointmentCountChart.setOption(appointmentOption);
      },
      (res: HttpErrorResponse) => {
        console.log(res.message);
      }
    );
  }

  loadAppointmentHourCountChart() {
    var hourAppointmentCountChart = echarts.init(document.getElementById('appointment-hour-count-stat'));

    var hourAppointmentStatOption = {
      title: {
        text: this.currentRegion + ' - ' + this.currentDate + '取号预约统计',
        x: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          // 坐标轴指示器，坐标轴触发有效
          type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
        }
      },
      legend: {
        top: '30px',
        data: ['排队中', '预约成功', '进厂', '离场']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '2%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: [
            '00',
            '01',
            '02',
            '03',
            '04',
            '05',
            '06',
            '07',
            '08',
            '09',
            '10',
            '11',
            '12',
            '13',
            '14',
            '15',
            '16',
            '17',
            '18',
            '19',
            '20',
            '21',
            '22',
            '23'
          ]
        }
      ],
      yAxis: [
        {
          type: 'value'
        }
      ],
      series: [
        // {
        //   name:'直接访问',
        //   type:'bar',
        //   data:[320, 332, 301, 334, 390, 330, 320]
        // },
        // {
        //   name:'邮件营销',
        //   type:'bar',
        //   stack: '广告',
        //   data:[120, 132, 101, 134, 90, 230, 210]
        // }
      ]
    };

    this.http.get(`${this.resourceUrl}/appointment-stats-today?currentRegion=${this.currentRegion}&date=${this.currentDate}`).subscribe(
      (res: HttpResponse<any>) => {
        let regionNames = [];
        for (let i = 0; i < res['data'].length; i++) {
          let region = res['data'][i]['region'];
          if (!regionNames.includes(region)) {
            regionNames.push(region);
          }
        }
        this.allRegions = regionNames;
        let statusKey = {
          排队中: 'wait',
          预约成功: 'start',
          进厂: 'enter',
          离场: 'leave'
        };
        for (let i = 0; i < hourAppointmentStatOption.legend.data.length; i++) {
          for (let j = 0; j < regionNames.length; j++) {
            let region = regionNames[j];
            if (region != this.currentRegion) {
              continue;
            }
            let item = {
              name: hourAppointmentStatOption.legend.data[i],
              type: 'bar',
              stack: region,
              data: []
            };
            for (let k = 0; k < 24; k++) {
              let hour = hourAppointmentStatOption.xAxis[0]['data'][k];
              for (let x = 0; x < res['data'].length; x++) {
                let e = res['data'][x];
                if (e['hour'] == hour && e['region'] == region) {
                  item.data.push(e[statusKey[item.name]]);
                  break;
                }
              }
            }
            hourAppointmentStatOption.series.push(item);
          }
        }
        hourAppointmentCountChart.setOption(hourAppointmentStatOption);
      },
      (res: HttpErrorResponse) => {
        console.log(res.message);
      }
    );
  }

  changeRegion(event) {
    this.loadAppointmentHourCountChart();
    this.initCharts();
  }

  changeDate(event) {
    this.loadAppointmentHourCountChart();
    this.initCharts();
  }

  ngOnInit() {
    // if (!this.accountService.isAuthenticated()) {
    //   this.router.navigateByUrl("/login");
    // }

    this.accountService.identity().then((account: Account) => {
      this.account = account;
      if (!this.account) {
        this.router.navigateByUrl('/login');
      }
    });

    this.registerAuthenticationSuccess();
    this.initCharts();
    this.loadAppointmentHourCountChart();
    this.loadLatestAppointments();

    let p = this;
    setInterval(function() {
      p.loadLatestAppointments();
    }, 5000);
  }

  registerAuthenticationSuccess() {
    this.eventManager.subscribe('authenticationSuccess', message => {
      this.accountService.identity().then(account => {
        this.account = account;
      });
    });
  }

  isAuthenticated() {
    return this.accountService.isAuthenticated();
  }

  login() {
    this.modalRef = this.loginModalService.open();
  }

  trackId(index: number, item: IAppointment) {
    return item.id;
  }
}
