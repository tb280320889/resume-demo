"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length,
        r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", {value: true});
var core_1 = require("@angular/core");
var testing_1 = require("@angular/http/testing");
var http_1 = require("@angular/http");
var ng_jhipster_1 = require("ng-jhipster");
var mock_language_service_1 = require("./helpers/mock-language.service");
var BlogTestModule = (function () {
    function BlogTestModule() {
    }

    BlogTestModule = __decorate([
        core_1.NgModule({
            providers: [
                testing_1.MockBackend,
                http_1.BaseRequestOptions,
                {
                    provide: ng_jhipster_1.JhiLanguageService,
                    useClass: mock_language_service_1.MockLanguageService
                },
                {
                    provide: http_1.Http,
                    useFactory: function (backendInstance, defaultOptions) {
                        return new http_1.Http(backendInstance, defaultOptions);
                    },
                    deps: [testing_1.MockBackend, http_1.BaseRequestOptions]
                }
            ]
        })
    ], BlogTestModule);
    return BlogTestModule;
}());
exports.BlogTestModule = BlogTestModule;
