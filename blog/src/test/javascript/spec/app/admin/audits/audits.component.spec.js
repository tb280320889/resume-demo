"use strict";
Object.defineProperty(exports, "__esModule", {value: true});
var testing_1 = require("@angular/core/testing");
var common_1 = require("@angular/common");
var ng_bootstrap_1 = require("@ng-bootstrap/ng-bootstrap");
var ng_jhipster_1 = require("ng-jhipster");
var test_module_1 = require("../../../test.module");
var uib_pagination_config_1 = require("../../../../../../main/webapp/app/blocks/config/uib-pagination.config");
var audits_component_1 = require("../../../../../../main/webapp/app/admin/audits/audits.component");
var audits_service_1 = require("../../../../../../main/webapp/app/admin/audits/audits.service");
var shared_1 = require("../../../../../../main/webapp/app/shared");

function getDate(isToday) {
    if (isToday === void 0) {
        isToday = true;
    }
    var date = new Date();
    if (isToday) {
        // Today + 1 day - needed if the current day must be included
        date.setDate(date.getDate() + 1);
    }
    else {
        // get last month
        if (date.getMonth() === 0) {
            date = new Date(date.getFullYear() - 1, 11, date.getDate());
        }
        else {
            date = new Date(date.getFullYear(), date.getMonth() - 1, date.getDate());
        }
    }
    return date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate();
}

describe('Component Tests', function () {
    describe('AuditsComponent', function () {
        var comp;
        var fixture;
        var service;
        beforeEach(testing_1.async(function () {
            testing_1.TestBed.configureTestingModule({
                imports: [test_module_1.BlogTestModule],
                declarations: [audits_component_1.AuditsComponent],
                providers: [
                    audits_service_1.AuditsService,
                    ng_bootstrap_1.NgbPaginationConfig,
                    ng_jhipster_1.JhiParseLinks,
                    uib_pagination_config_1.PaginationConfig,
                    common_1.DatePipe
                ]
            }).overrideTemplate(audits_component_1.AuditsComponent, '')
                .compileComponents();
        }));
        beforeEach(function () {
            fixture = testing_1.TestBed.createComponent(audits_component_1.AuditsComponent);
            comp = fixture.componentInstance;
            service = fixture.debugElement.injector.get(audits_service_1.AuditsService);
        });
        describe('today function ', function () {
            it('should set toDate to current date', function () {
                comp.today();
                expect(comp.toDate).toBe(getDate());
            });
        });
        describe('previousMonth function ', function () {
            it('should set fromDate to current date', function () {
                comp.previousMonth();
                expect(comp.fromDate).toBe(getDate(false));
            });
        });
        describe('By default, on init', function () {
            it('should set all default values correctly', function () {
                fixture.detectChanges();
                expect(comp.toDate).toBe(getDate());
                expect(comp.fromDate).toBe(getDate(false));
                expect(comp.itemsPerPage).toBe(shared_1.ITEMS_PER_PAGE);
                expect(comp.page).toBe(1);
                expect(comp.reverse).toBeFalsy();
                expect(comp.orderProp).toBe('timestamp');
            });
        });
    });
});
