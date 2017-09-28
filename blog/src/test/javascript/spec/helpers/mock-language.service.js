"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({__proto__: []} instanceof Array && function (d, b) {
            d.__proto__ = b;
        }) ||
        function (d, b) {
            for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        };
    return function (d, b) {
        extendStatics(d, b);

        function __() {
            this.constructor = d;
        }

        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
Object.defineProperty(exports, "__esModule", {value: true});
var spyobject_1 = require("./spyobject");
var ng_jhipster_1 = require("ng-jhipster");
var MockLanguageService = (function (_super) {
    __extends(MockLanguageService, _super);

    function MockLanguageService() {
        var _this = _super.call(this, ng_jhipster_1.JhiLanguageService) || this;
        _this.fakeResponse = 'en';
        _this.getCurrentSpy = _this.spy('getCurrent').andReturn(Promise.resolve(_this.fakeResponse));
        return _this;
    }

    MockLanguageService.prototype.init = function () {
    };
    MockLanguageService.prototype.changeLanguage = function (languageKey) {
    };
    MockLanguageService.prototype.setLocations = function (locations) {
    };
    MockLanguageService.prototype.addLocation = function (location) {
    };
    MockLanguageService.prototype.reload = function () {
    };
    return MockLanguageService;
}(spyobject_1.SpyObject));
exports.MockLanguageService = MockLanguageService;
