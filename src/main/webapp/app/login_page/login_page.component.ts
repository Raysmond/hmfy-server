import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';

import { LoginModalService, AccountService, Account, StateStorageService } from 'app/core';
import { LoginService } from 'app/core/login/login.service';

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

  constructor(
    private accountService: AccountService,
    private loginModalService: LoginModalService,
    private eventManager: JhiEventManager,
    private loginService: LoginService,
    private stateStorageService: StateStorageService,
    private http: HttpClient,
    private router: Router,
    private fb: FormBuilder
  ) {}

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
  }

  login() {
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
        let p = this;
        setTimeout(function() {
          // p.router.navigateByUrl("/");
          window.location.href = '/';
        }, 500);
      })
      .catch(() => {
        this.authenticationError = true;
      });
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
