define(['jquery',
        'underscore',
        'backbone',
        'goatApp/model/LessonOverviewCollection',
        'text!templates/paging_controls.html'],
    function ($,
              _,
              Backbone,
              LessonOverviewCollection,
              PaginationTemplate) {
        return Backbone.View.extend({
            template: PaginationTemplate,
            el: '#lesson-page-controls',

            initialize: function ($contentPages,baseLessonUrl,initPageNum) {
                this.$contentPages = $contentPages;
                this.collection = new LessonOverviewCollection();
                this.listenTo(this.collection, 'reset', this.render);
                this.numPages = this.$contentPages.length;
                this.baseUrl = baseLessonUrl;
                this.collection.fetch({reset:true});
                this.initPagination(initPageNum);
                //this.render();
             },

            render: function (e) {
                this.parseLinks();
                var t = _.template(this.template);
                this.$el.html(t({'overview':this.lessonOverview}));
                this.bindNavButtons();
                this.hideShowNavButtons();
            },

            updateCollection: function() {
                this.collection.fetch({reset:true});
            },

            bindNavButtons: function() {
                this.$el.find('span.glyphicon-class.glyphicon.glyphicon-circle-arrow-right.show-next-page').unbind().on('click',this.incrementPageView.bind(this));
                this.$el.find('span.glyphicon-class.glyphicon.glyphicon-circle-arrow-left.show-prev-page').unbind().on('click', this.decrementPageView.bind(this));
                this.navButtonsBound = true;
            },

            parseLinks: function() {
                var solvedMap = this.createSolvedMap();
                var pages = this.createPages(solvedMap);

                //assign to the view
                this.lessonOverview = {
                    baseUrl: this.baseUrl,
                    pages: pages
                }
            },

            createSolvedMap: function() {
                var solvedMap = {};
                _.each(this.collection.models, function(model) {
                    if (model.get('solved')) {
                        var key = model.get('assignment').path.replace(/\//g,'');
                        solvedMap[key] = model.get('assignment').name;
                    }
                });
                return solvedMap;
            },

            createPages: function(solvedMap) {
                var self = this;
                var pages = [];

                _.each(this.$contentPages, function(page, index) {
                    var curPageClass = (self.currentPage == index) ? ' cur-page' : '';
                    var pageInfo = self.getPageInfo(page, curPageClass, solvedMap);
                    pages.push(pageInfo);
                });

                return pages;
            },

            getPageInfo: function(page, curPageClass, solvedMap) {
                if ($(page).find('.attack-container').length < 1) {
                    return {content:'content', pageClass:'page-link', curPageClass:curPageClass};
                } else {
                    var $assignmentForms = $(page).find('.attack-container form.attack-form');
                    var solvedClass = this.getSolvedClass($assignmentForms, solvedMap);
                    return {solvedClass:solvedClass, content:'assignment', curPageClass:curPageClass, pageClass:'attack-link'};
                }
            },

            getSolvedClass: function($assignmentForms, solvedMap) {
                var solvedClass = 'solved-true';
                for (var i = 0; i < $assignmentForms.length; i++) {
                    var action = this.normalizeAction($assignmentForms.eq(i).attr('action'));
                    if (!(action && this.isAttackSolved(action, solvedMap))) {
                        solvedClass = 'solved-false';
                        break;
                    }
                }
                return solvedClass;
            },

            normalizeAction: function(action) {
                if (action.endsWith("WebWolf/mail/")) {
                    return "WebWolf/mail/send";
                }
                if (action.indexOf("?") > -1) {
                    return action.substring(0, action.indexOf("?"));
                }
                return action;
            },

            isAttackSolved: function(path, solvedMap) {
                var newPath = path.replace(/\//g,'');
                return typeof solvedMap[newPath] !== 'undefined';
            },

            showPrevPageButton: function() {
                $('span.glyphicon-class.glyphicon.glyphicon-circle-arrow-left.show-prev-page').show();
            },

            hidePrevPageButton: function() {
                $('span.glyphicon-class.glyphicon.glyphicon-circle-arrow-left.show-prev-page').hide();
            },

            showNextPageButton: function() {
                $('span.glyphicon-class.glyphicon.glyphicon-circle-arrow-right.show-next-page').show();
            },

            hideNextPageButton: function() {
                $('span.glyphicon-class.glyphicon.glyphicon-circle-arrow-right.show-next-page').hide();
            },

            initPagination: function(initPageNum) {
               //track pagination state in this view ... start at 0 .. unless a pageNum was provided
               this.currentPage = !initPageNum ? 0 : initPageNum;
            },

            setCurrentPage: function (pageNum) {
                this.currentPage = (_.isNumber(pageNum) && pageNum < this.numPages) ? pageNum : 0;
            },

            /* increment, decrement & display handlers */
            incrementPageView: function() {
                if (this.currentPage < this.numPages -1) {
                   this.currentPage++;
                   window.location.href = this.baseUrl + '/' + this.currentPage;
                }

                if (this.currentPage > 0) {
                    this.showPrevPageButton();
                }

                if (this.currentPage >= this.numPages -1) {
                    this.hideNextPageButton();
                    this.showPrevPageButton;
                }
                this.collection.fetch({reset:true});
            },

            decrementPageView: function() {
                if (this.currentPage > 0) {
                    this.currentPage--;
                    window.location.href = this.baseUrl + '/' + this.currentPage;
                }

                if (this.currentPage < this.numPages -1) {
                    this.showNextPageButton();
                }

                if (this.currentPage == 0) {
                    this.hidePrevPageButton();
                    this.showNextPageButton()
                }
                this.collection.fetch({reset:true});
            },

            hideShowNavButtons: function () {
                //one page only
                if (this.numPages === 1) {
                    this.hidePrevPageButton();
                    this.hideNextPageButton();
                }
                //first page
                if (this.currentPage === 0) {
                    this.hidePrevPageButton();
                    if (this.numPages > 1) {
                        this.showNextPageButton();
                    }
                    return;
                }
                // > first page, but not last
                if (this.currentPage > 0 && this.currentPage < this.numPages -1) {
                    this.showNextPageButton();
                    this.showPrevPageButton();
                    return;
                }
                // last page and more than one page
                if (this.currentPage === this.numPages -1 && this.numPages > 1) {
                    this.hideNextPageButton();
                    this.showPrevPageButton();
                    return;
                }

            },
        });
    });
