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
var platform_browser_1 = require("@angular/platform-browser");
var _1 = require("./");
var BlogSharedCommonModule = (function () {
    function BlogSharedCommonModule() {
    }

    BlogSharedCommonModule = __decorate([
        core_1.NgModule({
            imports: [
                _1.BlogSharedLibsModule
            ],
            declarations: [
                _1.FindLanguageFromKeyPipe,
                _1.JhiAlertComponent,
                _1.JhiAlertErrorComponent
            ],
            providers: [
                _1.JhiLanguageHelper,
                platform_browser_1.Title,
                {
                    provide: core_1.LOCALE_ID,
                    useValue: 'en'
                },
            ],
            exports: [
                _1.BlogSharedLibsModule,
                _1.FindLanguageFromKeyPipe,
                _1.JhiAlertComponent,
                _1.JhiAlertErrorComponent
            ]
        })
    ], BlogSharedCommonModule);
    return BlogSharedCommonModule;
}());
exports.BlogSharedCommonModule = BlogSharedCommonModule;
