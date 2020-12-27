import {Component, OnInit} from '@angular/core';
import {NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {JhiEventManager} from 'ng-jhipster';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router, NavigationEnd} from '@angular/router';
import {ActivatedRoute} from '@angular/router';

import {HttpClient, HttpResponse, HttpErrorResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {SERVER_API_URL} from 'app/app.constants';

import {LoginModalService, AccountService, Account, StateStorageService} from 'app/core';
import {LoginService} from 'app/core/login/login.service';

@Component({
  selector: 'jhi-login-page',
  templateUrl: './login.component.html',
  styleUrls: ['login.scss']
})
export class LoginPageComponent implements OnInit {
  account: Account;
  modalRef: NgbModalRef;

  authenticationError: boolean;

  loginForm = this.fb.group({
    username: [''],
    password: [''],
    rememberMe: [true]
  });

  unionAlert: string = '';
  accessToken: string;
  private sub: any;

  constructor(
    private accountService: AccountService,
    private loginModalService: LoginModalService,
    private eventManager: JhiEventManager,
    private loginService: LoginService,
    private stateStorageService: StateStorageService,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
  }

  cancel() {
    this.authenticationError = false;
    this.loginForm.patchValue({
      username: '',
      password: ''
    });
  }

  ngOnInit() {
    this.accountService.identity().then((account: Account) => {
      this.account = account;
    });
    this.registerAuthenticationSuccess();
    this.sub = this.route.queryParams.subscribe(params => {
      this.accessToken = params['token'];
      this.loginWithAccessToken();
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  loginWithAccessToken() {
    console.log('start to login with union access_token: ' + this.accessToken);
    this.unionAlert = "统一身份认证成功，登录中..";
    this.loginService.loginWithUnionToken(this.accessToken, this.loginForm.get('rememberMe').value)
      .then(() => {
        console.log('union login success:')
        this.unionAlert = '登录成功！';
        this.authenticationError = false;

        this.eventManager.broadcast({
          name: 'authenticationSuccess',
          content: 'Sending Authentication Success'
        });

        setTimeout(function () {
          // p.router.navigateByUrl("/");
          window.location.href = '/';
        }, 500);
      })
      .catch((err) => {
        console.log(err);
        this.authenticationError = true;
        this.unionAlert = '';
        if (err.error.reason == "未绑定统一身份认证") {
          this.unionAlert = "未绑定统一身份认证，登录后即可绑定！";
        }
      })
  }

  login() {
    let p = this;
    this.loginService
      .login({
        username: this.loginForm.get('username').value,
        password: this.loginForm.get('password').value,
        rememberMe: this.loginForm.get('rememberMe').value
      })
      .then(() => {
        this.authenticationError = false;
        // if (this.router.url === '/register' || /^\/activate\//.test(this.router.url) || /^\/reset\//.test(this.router.url)) {
        //   this.router.navigate(['']);
        // }

        if (p.accessToken) {

          this.loginService.loginWithUnionToken(p.accessToken, p.loginForm.get('rememberMe').value).then(() => {
            p.unionAlert = "绑定统一身份认证成功！";
            p.afterLoginSuccess();
          })
            .catch(() => {
              alert("绑定统一身份失败！")
              p.afterLoginSuccess();
            })
        } else {
          p.afterLoginSuccess();
        }



      })
      .catch((err) => {
        this.authenticationError = true;
      });
  }

  afterLoginSuccess() {
    this.eventManager.broadcast({
      name: 'authenticationSuccess',
      content: 'Sending Authentication Success'
    });

    // previousState was set in the authExpiredInterceptor before being redirected to login modal.
    // since login is successful, go to stored previousState and clear previousState
    // const redirect = this.stateStorageService.getUrl();
    // if (redirect) {
    //   this.stateStorageService.storeUrl(null);
    //   this.router.navigateByUrl(redirect);
    // }
    setTimeout(function () {
      // p.router.navigateByUrl("/");
      window.location.href = '/';
    }, 500);
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
}
