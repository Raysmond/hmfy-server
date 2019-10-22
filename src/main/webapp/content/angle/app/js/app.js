/**
 * This library was created to emulate some jQuery features
 * used in this template only with Javascript and DOM
 * manipulation functions (IE10+).
 * All methods were designed for an adequate and specific use
 * and don't perform a deep validation on the arguments provided.
 *
 * IMPORTANT:
 * ==========
 * It's suggested NOT to use this library extensively unless you
 * understand what each method does. Instead, use only JS or
 * you might even need jQuery.
 */

(function(global, factory) {
    if (typeof exports === 'object') { // CommonJS-like
        module.exports = factory();
    } else { // Browser
        if (typeof global.jQuery === 'undefined')
            global.$ = factory();
    }
}(this, function() {

    // HELPERS
    function arrayFrom(obj) {
        return ('length' in obj) && (obj !== window) ? [].slice.call(obj) : [obj];
    }

    function filter(ctx, fn) {
        return [].filter.call(ctx, fn);
    }

    function map(ctx, fn) {
        return [].map.call(ctx, fn);
    }

    function matches(item, selector) {
        return (Element.prototype.matches || Element.prototype.msMatchesSelector).call(item, selector)
    }

    // Events handler with simple scoped events support
    var EventHandler = function() {
        this.events = {};
    }
    EventHandler.prototype = {
        // event accepts: 'click' or 'click.scope'
        bind: function(event, listener, target) {
            var type = event.split('.')[0];
            target.addEventListener(type, listener, false);
            this.events[event] = {
                type: type,
                listener: listener
            }
        },
        unbind: function(event, target) {
            if (event in this.events) {
                target.removeEventListener(this.events[event].type, this.events[event].listener, false);
                delete this.events[event];
            }
        }
    }

    // Object Definition
    var Wrap = function(selector) {
        this.selector = selector;
        return this._setup([]);
    }

    // CONSTRUCTOR
    Wrap.Constructor = function(param, attrs) {
        var el = new Wrap(param);
        return el.init(attrs);
    };

    // Core methods
    Wrap.prototype = {
        constructor: Wrap,
        /**
         * Initialize the object depending on param type
         * [attrs] only to handle $(htmlString, {attributes})
         */
        init: function(attrs) {
            // empty object
            if (!this.selector) return this;
            // selector === string
            if (typeof this.selector === 'string') {
                // if looks like markup, try to create an element
                if (this.selector[0] === '<') {
                    var elem = this._setup([this._create(this.selector)])
                    return attrs ? elem.attr(attrs) : elem;
                } else
                    return this._setup(arrayFrom(document.querySelectorAll(this.selector)))
            }
            // selector === DOMElement
            if (this.selector.nodeType)
                return this._setup([this.selector])
            else // shorthand for DOMReady
                if (typeof this.selector === 'function')
                    return this._setup([document]).ready(this.selector)
            // Array like objects (e.g. NodeList/HTMLCollection)
            return this._setup(arrayFrom(this.selector))
        },
        /**
         * Creates a DOM element from a string
         * Strictly supports the form: '<tag>' or '<tag/>'
         */
        _create: function(str) {
            var nodeName = str.substr(str.indexOf('<') + 1, str.indexOf('>') - 1).replace('/', '')
            return document.createElement(nodeName);
        },
        /** setup properties and array to element set */
        _setup: function(elements) {
            var i = 0;
            for (; i < elements.length; i++) delete this[i]; // clean up old set
            this.elements = elements;
            this.length = elements.length;
            for (i = 0; i < elements.length; i++) this[i] = elements[i] // new set
            return this;
        },
        _first: function(cb, ret) {
            var f = this.elements[0];
            return f ? (cb ? cb.call(this, f) : f) : ret;
        },
        /** Common function for class manipulation  */
        _classes: function(method, classname) {
            var cls = classname.split(' ');
            if (cls.length > 1) {
                cls.forEach(this._classes.bind(this, method))
            } else {
                if (method === 'contains') {
                    var elem = this._first();
                    return elem ? elem.classList.contains(classname) : false;
                }
                return (classname === '') ? this : this.each(function(i, item) {
                    item.classList[method](classname);
                })
            }
        },
        /**
         * Multi purpose function to set or get a (key, value)
         * If no value, works as a getter for the given key
         * key can be an object in the form {key: value, ...}
         */
        _access: function(key, value, fn) {
            if (typeof key === 'object') {
                for (var k in key) {
                    this._access(k, key[k], fn);
                }
            } else if (value === undefined) {
                return this._first(function(elem) {
                    return fn(elem, key);
                });
            }
            return this.each(function(i, item) {
                fn(item, key, value);
            });
        },
        each: function(fn, arr) {
            arr = arr ? arr : this.elements;
            for (var i = 0; i < arr.length; i++) {
                if (fn.call(arr[i], i, arr[i]) === false)
                    break;
            }
            return this;
        }
    }

    /** Allows to extend with new methods */
    Wrap.extend = function(methods) {
        Object.keys(methods).forEach(function(m) {
            Wrap.prototype[m] = methods[m]
        })
    }

    // DOM READY
    Wrap.extend({
        ready: function(fn) {
            if (document.attachEvent ? document.readyState === 'complete' : document.readyState !== 'loading') {
                fn();
            } else {
                document.addEventListener('DOMContentLoaded', fn);
            }
            return this;
        }
    })
    // ACCESS
    Wrap.extend({
        /** Get or set a css value */
        css: function(key, value) {
            var getStyle = function(e, k) { return e.style[k] || getComputedStyle(e)[k]; };
            return this._access(key, value, function(item, k, val) {
                var unit = (typeof val === 'number') ? 'px' : '';
                return val === undefined ? getStyle(item, k) : (item.style[k] = val + unit);
            })
        },
        /** Get an attribute or set it */
        attr: function(key, value) {
            return this._access(key, value, function(item, k, val) {
                return val === undefined ? item.getAttribute(k) : item.setAttribute(k, val)
            })
        },
        /** Get a property or set it */
        prop: function(key, value) {
            return this._access(key, value, function(item, k, val) {
                return val === undefined ? item[k] : (item[k] = val);
            })
        },
        position: function() {
            return this._first(function(elem) {
                return { left: elem.offsetLeft, top: elem.offsetTop }
            });
        },
        scrollTop: function(value) {
            return this._access('scrollTop', value, function(item, k, val) {
                return val === undefined ? item[k] : (item[k] = val);
            })
        },
        outerHeight: function(includeMargin) {
            return this._first(function(elem) {
                var style = getComputedStyle(elem);
                var margins = includeMargin ? (parseInt(style.marginTop, 10) + parseInt(style.marginBottom, 10)) : 0;
                return elem.offsetHeight + margins;
            });
        },
        /**
         * Find the position of the first element in the set
         * relative to its sibling elements.
         */
        index: function() {
            return this._first(function(el) {
                return arrayFrom(el.parentNode.children).indexOf(el)
            }, -1);
        }
    })
    // LOOKUP
    Wrap.extend({
        children: function(selector) {
            var childs = [];
            this.each(function(i, item) {
                childs = childs.concat(map(item.children, function(item) {
                    return item
                }))
            })
            return Wrap.Constructor(childs).filter(selector);
        },
        siblings: function() {
            var sibs = []
            this.each(function(i, item) {
                sibs = sibs.concat(filter(item.parentNode.children, function(child) {
                    return child !== item;
                }))
            })
            return Wrap.Constructor(sibs)
        },
        /** Return the parent of each element in the current set */
        parent: function() {
            var par = map(this.elements, function(item) {
                return item.parentNode;
            })
            return Wrap.Constructor(par)
        },
        /** Return ALL parents of each element in the current set */
        parents: function(selector) {
            var par = [];
            this.each(function(i, item) {
                for (var p = item.parentElement; p; p = p.parentElement)
                    par.push(p);
            })
            return Wrap.Constructor(par).filter(selector)
        },
        /**
         * Get the descendants of each element in the set, filtered by a selector
         * Selector can't start with ">" (:scope not supported on IE).
         */
        find: function(selector) {
            var found = []
            this.each(function(i, item) {
                found = found.concat(map(item.querySelectorAll( /*':scope ' + */ selector), function(fitem) {
                    return fitem
                }))
            })
            return Wrap.Constructor(found)
        },
        /** filter the actual set based on given selector */
        filter: function(selector) {
            if (!selector) return this;
            var res = filter(this.elements, function(item) {
                return matches(item, selector)
            })
            return Wrap.Constructor(res)
        },
        /** Works only with a string selector */
        is: function(selector) {
            var found = false;
            this.each(function(i, item) {
                return !(found = matches(item, selector))
            })
            return found;
        }
    });
    // ELEMENTS
    Wrap.extend({
        /**
         * append current set to given node
         * expects a dom node or set
         * if element is a set, prepends only the first
         */
        appendTo: function(elem) {
            elem = elem.nodeType ? elem : elem._first()
            return this.each(function(i, item) {
                elem.appendChild(item);
            })
        },
        /**
         * Append a domNode to each element in the set
         * if element is a set, append only the first
         */
        append: function(elem) {
            elem = elem.nodeType ? elem : elem._first()
            return this.each(function(i, item) {
                item.appendChild(elem);
            })
        },
        /**
         * Insert the current set of elements after the element
         * that matches the given selector in param
         */
        insertAfter: function(selector) {
            var target = document.querySelector(selector);
            return this.each(function(i, item) {
                target.parentNode.insertBefore(item, target.nextSibling);
            })
        },
        /**
         * Clones all element in the set
         * returns a new set with the cloned elements
         */
        clone: function() {
            var clones = map(this.elements, function(item) {
                return item.cloneNode(true)
            })
            return Wrap.Constructor(clones);
        },
        /** Remove all node in the set from DOM. */
        remove: function() {
            this.each(function(i, item) {
                delete item.events;
                delete item.data;
                if (item.parentNode) item.parentNode.removeChild(item);
            })
            this._setup([])
        }
    })
    // DATASETS
    Wrap.extend({
        /**
         * Expected key in camelCase format
         * if value provided save data into element set
         * if not, return data for the first element
         */
        data: function(key, value) {
            var hasJSON = /^(?:\{[\w\W]*\}|\[[\w\W]*\])$/,
                dataAttr = 'data-' + key.replace(/[A-Z]/g, '-$&').toLowerCase();
            if (value === undefined) {
                return this._first(function(el) {
                    if (el.data && el.data[key])
                        return el.data[key];
                    else {
                        var data = el.getAttribute(dataAttr)
                        if (data === 'true') return true;
                        if (data === 'false') return false;
                        if (data === +data + '') return +data;
                        if (hasJSON.test(data)) return JSON.parse(data);
                        return data;
                    }
                });
            } else {
                return this.each(function(i, item) {
                    item.data = item.data || {};
                    item.data[key] = value;
                });
            }
        }
    })
    // EVENTS
    Wrap.extend({
        trigger: function(type) {
            type = type.split('.')[0]; // ignore namespace
            var event = document.createEvent('HTMLEvents');
            event.initEvent(type, true, false);
            return this.each(function(i, item) {
                item.dispatchEvent(event);
            })
        },
        blur: function() {
            return this.trigger('blur')
        },
        focus: function() {
            return this.trigger('focus')
        },
        on: function(event, callback) {
            return this.each(function(i, item) {
                if (!item.events) item.events = new EventHandler();
                event.split(' ').forEach(function(ev) {
                    item.events.bind(ev, callback, item);
                })
            })
        },
        off: function(event) {
            return this.each(function(i, item) {
                if (item.events) {
                    item.events.unbind(event, item);
                    delete item.events;
                }
            })
        }
    })
    // CLASSES
    Wrap.extend({
        toggleClass: function(classname) {
            return this._classes('toggle', classname);
        },
        addClass: function(classname) {
            return this._classes('add', classname);
        },
        removeClass: function(classname) {
            return this._classes('remove', classname);
        },
        hasClass: function(classname) {
            return this._classes('contains', classname);
        }
    })


    /**
     * Some basic features in this template relies on Bootstrap
     * plugins, like Collapse, Dropdown and Tab.
     * Below code emulates plugins behavior by toggling classes
     * from elements to allow a minimum interaction without animation.
     * - Only Collapse is required which is used by the sidebar.
     * - Tab and Dropdown are optional features.
     */

    // Emulate jQuery symbol to simplify usage
    var $ = Wrap.Constructor;

    // Emulates Collapse plugin
    Wrap.extend({
        collapse: function(action) {
            return this.each(function(i, item) {
                var $item = $(item).trigger(action + '.bs.collapse');
                if (action === 'toggle') $item.collapse($item.hasClass('show') ? 'hide' : 'show');
                else $item[action === 'show' ? 'addClass' : 'removeClass']('show');
            })
        }
    })
    // Initializations
    $('[data-toggle]').on('click', function(e) {
        var target = $(e.currentTarget);
        if (target.is('a')) e.preventDefault();
        switch (target.data('toggle')) {
            case 'collapse':
                $(target.attr('href')).collapse('toggle');
                break;
            case 'tab':
                target.parent().parent().find('.active').removeClass('active');
                target.addClass('active');
                var tabPane = $(target.attr('href'));
                tabPane.siblings().removeClass('active show');
                tabPane.addClass('active show');
                break;
            case 'dropdown':
                var dd = target.parent().toggleClass('show');
                dd.find('.dropdown-menu').toggleClass('show');
                break;
            default:
                break;
        }
    })


    return Wrap.Constructor

}));
/*!
 *
 * Angle - Bootstrap Admin Template
 *
 * Version: 4.3
 * Author: @themicon_co
 * Website: http://themicon.co
 * License: https://wrapbootstrap.com/help/licenses
 *
 */


(function() {
    'use strict';

    $(function() {

        // Restore body classes
        // -----------------------------------
        var $body = $('body');
        new StateToggler().restoreState($body);

        // enable settings toggle after restore
        $('#chk-fixed').prop('checked', $body.hasClass('layout-fixed'));
        $('#chk-collapsed').prop('checked', $body.hasClass('aside-collapsed'));
        $('#chk-collapsed-text').prop('checked', $body.hasClass('aside-collapsed-text'));
        $('#chk-boxed').prop('checked', $body.hasClass('layout-boxed'));
        $('#chk-float').prop('checked', $body.hasClass('aside-float'));
        $('#chk-hover').prop('checked', $body.hasClass('aside-hover'));

        // When ready display the offsidebar
        $('.offsidebar.d-none').removeClass('d-none');

        // DEMO ONLY: remove sidebar related classes as they dont
        // have effect with horizontal layout and disabled switches
        if (document.body.className.indexOf('layout-h') > -1) {
            document.body.className = document.body.className.replace(/(^|\s)aside-\S+/g, '');
            $('#chk-collapsed').prop({ disabled: true, checked: false });
            $('#chk-collapsed-text').prop({ disabled: true, checked: false });
            $('#chk-float').prop({ disabled: true, checked: false });
            $('#chk-hover').prop({ disabled: true, checked: false });
            $('#chk-scroll').prop({ disabled: true, checked: false });
        }

    }); // doc ready

})();
// Start Bootstrap JS
// -----------------------------------

(function() {
    'use strict';

    $(initBootstrap);

    function initBootstrap() {

        // necessary check at least til BS doesn't require jQuery
        if (!$.fn || !$.fn.tooltip || !$.fn.popover) return;

        // POPOVER
        // -----------------------------------

        $('[data-toggle="popover"]').popover();

        // TOOLTIP
        // -----------------------------------

        $('[data-toggle="tooltip"]').tooltip({
            container: 'body'
        });

        // DROPDOWN INPUTS
        // -----------------------------------
        $('.dropdown input').on('click focus', function(event) {
            event.stopPropagation();
        });

    }

})();
// Module: card-tools
// -----------------------------------

(function() {
    'use strict';

    $(initCardDismiss);
    $(initCardCollapse);
    $(initCardRefresh);


    /**
     * Helper function to find the closest
     * ascending .card element
     */
    function getCardParent(item) {
        var el = item.parentElement;
        while (el && !el.classList.contains('card'))
            el = el.parentElement
        return el
    }
    /**
     * Helper to trigger custom event
     */
    function triggerEvent(type, item, data) {
        var ev;
        if (typeof CustomEvent === 'function') {
            ev = new CustomEvent(type, { detail: data });
        } else {
            ev = document.createEvent('CustomEvent');
            ev.initCustomEvent(type, true, false, data);
        }
        item.dispatchEvent(ev);
    }

    /**
     * Dismiss cards
     * [data-tool="card-dismiss"]
     */
    function initCardDismiss() {
        var cardtoolSelector = '[data-tool="card-dismiss"]'

        var cardList = [].slice.call(document.querySelectorAll(cardtoolSelector))

        cardList.forEach(function(item) {
            new CardDismiss(item);
        })

        function CardDismiss(item) {
            var EVENT_REMOVE = 'card.remove';
            var EVENT_REMOVED = 'card.removed';

            this.item = item;
            this.cardParent = getCardParent(this.item);
            this.removing = false; // prevents double execution

            this.clickHandler = function(e) {
                if (this.removing) return;
                this.removing = true;
                // pass callbacks via event.detail to confirm/cancel the removal
                triggerEvent(EVENT_REMOVE, this.cardParent, {
                    confirm: this.confirm.bind(this),
                    cancel: this.cancel.bind(this)
                });
            }
            this.confirm = function() {
                this.animate(this.cardParent, function() {
                    triggerEvent(EVENT_REMOVED, this.cardParent);
                    this.remove(this.cardParent);
                })
            }
            this.cancel = function() {
                this.removing = false;
            }
            this.animate = function(item, cb) {
                if ('onanimationend' in window) { // animation supported
                    item.addEventListener('animationend', cb.bind(this))
                    item.className += ' animated bounceOut'; // requires animate.css
                } else cb.call(this) // no animation, just remove
            }
            this.remove = function(item) {
                item.parentNode.removeChild(item);
            }
            // attach listener
            item.addEventListener('click', this.clickHandler.bind(this), false)
        }
    }


    /**
     * Collapsed cards
     * [data-tool="card-collapse"]
     * [data-start-collapsed]
     */
    function initCardCollapse() {
        var cardtoolSelector = '[data-tool="card-collapse"]';
        var cardList = [].slice.call(document.querySelectorAll(cardtoolSelector))

        cardList.forEach(function(item) {
            var initialState = item.hasAttribute('data-start-collapsed')
            new CardCollapse(item, initialState);
        })

        function CardCollapse(item, startCollapsed) {
            var EVENT_SHOW = 'card.collapse.show';
            var EVENT_HIDE = 'card.collapse.hide';

            this.state = true; // true -> show / false -> hide
            this.item = item;
            this.cardParent = getCardParent(this.item);
            this.wrapper = this.cardParent.querySelector('.card-wrapper');

            this.toggleCollapse = function(action) {
                triggerEvent(action ? EVENT_SHOW : EVENT_HIDE, this.cardParent)
                this.wrapper.style.maxHeight = (action ? this.wrapper.scrollHeight : 0) + 'px'
                this.state = action;
                this.updateIcon(action)
            }
            this.updateIcon = function(action) {
                this.item.firstElementChild.className = action ? 'fa fa-minus' : 'fa fa-plus'
            }
            this.clickHandler = function() {
                this.toggleCollapse(!this.state);
            }
            this.initStyles = function() {
                this.wrapper.style.maxHeight = this.wrapper.scrollHeight + 'px';
                this.wrapper.style.transition = 'max-height 0.5s';
                this.wrapper.style.overflow = 'hidden';
            }

            // prepare styles for collapse animation
            this.initStyles()
            // set initial state if provided
            if (startCollapsed) {
                this.toggleCollapse(false)
            }
            // attach listener
            this.item.addEventListener('click', this.clickHandler.bind(this), false)

        }
    }


    /**
     * Refresh cards
     * [data-tool="card-refresh"]
     * [data-spinner="standard"]
     */
    function initCardRefresh() {

        var cardtoolSelector = '[data-tool="card-refresh"]';
        var cardList = [].slice.call(document.querySelectorAll(cardtoolSelector))

        cardList.forEach(function(item) {
            new CardRefresh(item);
        })

        function CardRefresh(item) {
            var EVENT_REFRESH = 'card.refresh';
            var WHIRL_CLASS = 'whirl';
            var DEFAULT_SPINNER = 'standard'

            this.item = item;
            this.cardParent = getCardParent(this.item)
            this.spinner = ((this.item.dataset || {}).spinner || DEFAULT_SPINNER).split(' '); // support space separated classes

            this.refresh = function(e) {
                var card = this.cardParent;
                // start showing the spinner
                this.showSpinner(card, this.spinner)
                // attach as public method
                card.removeSpinner = this.removeSpinner.bind(this);
                // Trigger the event and send the card
                triggerEvent(EVENT_REFRESH, card, { card: card });
            }
            this.showSpinner = function(card, spinner) {
                card.classList.add(WHIRL_CLASS);
                spinner.forEach(function(s) { card.classList.add(s) })
            }
            this.removeSpinner = function() {
                this.cardParent.classList.remove(WHIRL_CLASS);
            }

            // attach listener
            this.item.addEventListener('click', this.refresh.bind(this), false)

        }
    }

})();
// GLOBAL CONSTANTS
// -----------------------------------

(function() {

    window.APP_COLORS = {
        'primary':                '#5d9cec',
        'success':                '#27c24c',
        'info':                   '#23b7e5',
        'warning':                '#ff902b',
        'danger':                 '#f05050',
        'inverse':                '#131e26',
        'green':                  '#37bc9b',
        'pink':                   '#f532e5',
        'purple':                 '#7266ba',
        'dark':                   '#3a3f51',
        'yellow':                 '#fad732',
        'gray-darker':            '#232735',
        'gray-dark':              '#3a3f51',
        'gray':                   '#dde6e9',
        'gray-light':             '#e4eaec',
        'gray-lighter':           '#edf1f2'
    };

    window.APP_MEDIAQUERY = {
        'desktopLG':             1200,
        'desktop':                992,
        'tablet':                 768,
        'mobile':                 480
    };

})();
// FULLSCREEN
// -----------------------------------

(function() {
    'use strict';

    $(initScreenFull);

    function initScreenFull() {
        if (typeof screenfull === 'undefined') return;

        var $doc = $(document);
        var $fsToggler = $('[data-toggle-fullscreen]');

        // Not supported under IE
        var ua = window.navigator.userAgent;
        if (ua.indexOf("MSIE ") > 0 || !!ua.match(/Trident.*rv\:11\./)) {
            $fsToggler.addClass('d-none'); // hide element
            return; // and abort
        }

        $fsToggler.on('click', function(e) {
            e.preventDefault();

            if (screenfull.enabled) {

                screenfull.toggle();

                // Switch icon indicator
                toggleFSIcon($fsToggler);

            } else {
                console.log('Fullscreen not enabled');
            }
        });

        if (screenfull.raw && screenfull.raw.fullscreenchange)
            $doc.on(screenfull.raw.fullscreenchange, function() {
                toggleFSIcon($fsToggler);
            });

        function toggleFSIcon($element) {
            if (screenfull.isFullscreen)
                $element.children('em').removeClass('fa-expand').addClass('fa-compress');
            else
                $element.children('em').removeClass('fa-compress').addClass('fa-expand');
        }

    }

})();
// LOAD CUSTOM CSS
// -----------------------------------

(function() {
    'use strict';

    $(initLoadCSS);

    function initLoadCSS() {

        $('[data-load-css]').on('click', function(e) {

            var element = $(this);

            if (element.is('a'))
                e.preventDefault();

            var uri = element.data('loadCss'),
                link;

            if (uri) {
                link = createLink(uri);
                if (!link) {
                    $.error('Error creating stylesheet link element.');
                }
            } else {
                $.error('No stylesheet location defined.');
            }

        });
    }

    function createLink(uri) {
        var linkId = 'autoloaded-stylesheet',
            oldLink = $('#' + linkId).attr('id', linkId + '-old');

        $('head').append($('<link/>').attr({
            'id': linkId,
            'rel': 'stylesheet',
            'href': uri
        }));

        if (oldLink.length) {
            oldLink.remove();
        }

        return $('#' + linkId);
    }

})();
// TRANSLATION
// -----------------------------------

(function() {
    'use strict';

    // $(initTranslation);


    var pathPrefix = 'server/i18n'; // folder of json files
    var STORAGEKEY = 'jq-appLang';
    var savedLanguage = Storages.localStorage.get(STORAGEKEY);

    function initTranslation() {
        i18next
            .use(i18nextXHRBackend)
            // .use(LanguageDetector)
            .init({
                fallbackLng: savedLanguage || 'en',
                backend: {
                    loadPath: pathPrefix + '/{{ns}}-{{lng}}.json',
                },
                ns: ['site'],
                defaultNS: 'site',
                debug: false
            }, function(err, t) {
                // initialize elements
                applyTranlations();
                // listen to language changes
                attachChangeListener();
            })

        function applyTranlations() {
            var list = [].slice.call(document.querySelectorAll('[data-localize]'))
            list.forEach(function(item) {
                var key = item.getAttribute('data-localize')
                if (i18next.exists(key)) item.innerHTML = i18next.t(key);
            })
        }

        function attachChangeListener() {
            var list = [].slice.call(document.querySelectorAll('[data-set-lang]'))
            list.forEach(function(item) {

                item.addEventListener('click', function(e) {
                    if (e.target.tagName === 'A') e.preventDefault();
                    var lang = item.getAttribute('data-set-lang')
                    if (lang) {
                        i18next.changeLanguage(lang, function(err) {
                            if (err) console.log(err)
                            else {
                                applyTranlations();
                                Storages.localStorage.set(STORAGEKEY, lang);
                            }
                        });
                    }
                    activateDropdown(item)
                });

            })
        }

        function activateDropdown(item) {
            if (item.classList.contains('dropdown-item')) {
                item.parentElement.previousElementSibling.innerHTML = item.innerHTML;
            }
        }

    }


})();
// NAVBAR SEARCH
// -----------------------------------

(function() {
    'use strict';

    $(initNavbarSearch);

    function initNavbarSearch() {

        var navSearch = new navbarSearchInput();

        // Open search input
        var $searchOpen = $('[data-search-open]');

        $searchOpen
            .on('click', function(e) { e.stopPropagation(); })
            .on('click', navSearch.toggle);

        // Close search input
        var $searchDismiss = $('[data-search-dismiss]');
        var inputSelector = '.navbar-form input[type="text"]';

        $(inputSelector)
            .on('click', function(e) { e.stopPropagation(); })
            .on('keyup', function(e) {
                if (e.keyCode == 27) // ESC
                    navSearch.dismiss();
            });

        // click anywhere closes the search
        $(document).on('click', navSearch.dismiss);
        // dismissable options
        $searchDismiss
            .on('click', function(e) { e.stopPropagation(); })
            .on('click', navSearch.dismiss);

    }

    var navbarSearchInput = function() {
        var navbarFormSelector = 'form.navbar-form';
        return {
            toggle: function() {

                var navbarForm = $(navbarFormSelector);

                navbarForm.toggleClass('open');

                var isOpen = navbarForm.hasClass('open');

                navbarForm.find('input')[isOpen ? 'focus' : 'blur']();

            },

            dismiss: function() {
                $(navbarFormSelector)
                    .removeClass('open') // Close control
                    .find('input[type="text"]').blur() // remove focus
                // .val('')                    // Empty input
                ;
            }
        };

    }

})();
// NOW TIMER
// -----------------------------------

(function() {
    'use strict';

    $(initNowTimer);

    function initNowTimer() {

        if (typeof moment === 'undefined') return;

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

})();
// Toggle RTL mode for demo
// -----------------------------------


(function() {
    'use strict';

    $(initRTL);

    function initRTL() {
        var maincss = $('#maincss');
        var bscss = $('#bscss');
        $('#chk-rtl').on('change', function() {
            // app rtl check
            maincss.attr('href', this.checked ? 'css/app-rtl.css' : 'css/app.css');
            // bootstrap rtl check
            bscss.attr('href', this.checked ? 'css/bootstrap-rtl.css' : 'css/bootstrap.css');
        });
    }

})();
// SIDEBAR
// -----------------------------------


(function() {
    'use strict';

    $(initSidebar);

    var $html;
    var $body;
    var $sidebar;

    function initSidebar() {

        $html = $('html');
        $body = $('body');
        $sidebar = $('.sidebar');

        // AUTOCOLLAPSE ITEMS
        // -----------------------------------

        var sidebarCollapse = $sidebar.find('.collapse');
        sidebarCollapse.on('show.bs.collapse', function(event) {

            event.stopPropagation();
            if ($(this).parents('.collapse').length === 0)
                sidebarCollapse.filter('.show').collapse('hide');

        });

        // SIDEBAR ACTIVE STATE
        // -----------------------------------

        // Find current active item
        var currentItem = $('.sidebar .active').parents('li');

        // hover mode don't try to expand active collapse
        // if (!useAsideHover())
        //     currentItem
        //     .addClass('active') // activate the parent
        //     .children('.collapse') // find the collapse
        //     .collapse('show'); // and show it

        // remove this if you use only collapsible sidebar items
        $sidebar.find('li > a + ul').on('show.bs.collapse', function(e) {
            if (useAsideHover()) e.preventDefault();
        });

        // SIDEBAR COLLAPSED ITEM HANDLER
        // -----------------------------------


        var eventName = isTouch() ? 'click' : 'mouseenter';
        var subNav = $();
        $sidebar.find('.sidebar-nav > li').on(eventName, function(e) {

            if (isSidebarCollapsed() || useAsideHover()) {

                subNav.trigger('mouseleave');
                subNav = toggleMenuItem($(this));

                // Used to detect click and touch events outside the sidebar
                sidebarAddBackdrop();
            }

        });

        var sidebarAnyclickClose = $sidebar.data('sidebarAnyclickClose');

        // Allows to close
        if (typeof sidebarAnyclickClose !== 'undefined') {

            $('.wrapper').on('click.sidebar', function(e) {
                // don't check if sidebar not visible
                if (!$body.hasClass('aside-toggled')) return;

                var $target = $(e.target);
                if (!$target.parents('.aside-container').length && // if not child of sidebar
                    !$target.is('#user-block-toggle') && // user block toggle anchor
                    !$target.parent().is('#user-block-toggle') // user block toggle icon
                ) {
                    $body.removeClass('aside-toggled');
                }

            });
        }
    }

    function sidebarAddBackdrop() {
        var $backdrop = $('<div/>', { 'class': 'sidebar-backdrop' });
        $backdrop.insertAfter('.aside-container').on("click mouseenter", function() {
            removeFloatingNav();
        });
    }

    // Open the collapse sidebar submenu items when on touch devices
    // - desktop only opens on hover
    function toggleTouchItem($element) {
        $element
            .siblings('li')
            .removeClass('open')
        $element
            .toggleClass('open');
    }

    // Handles hover to open items under collapsed menu
    // -----------------------------------
    function toggleMenuItem($listItem) {

        removeFloatingNav();

        var ul = $listItem.children('ul');

        if (!ul.length) return $();
        if ($listItem.hasClass('open')) {
            toggleTouchItem($listItem);
            return $();
        }

        var $aside = $('.aside-container');
        var $asideInner = $('.aside-inner'); // for top offset calculation
        // float aside uses extra padding on aside
        var mar = parseInt($asideInner.css('padding-top'), 0) + parseInt($aside.css('padding-top'), 0);

        var subNav = ul.clone().appendTo($aside);

        toggleTouchItem($listItem);

        var itemTop = ($listItem.position().top + mar) - $sidebar.scrollTop();
        var vwHeight = document.body.clientHeight;

        subNav
            .addClass('nav-floating')
            .css({
                position: isFixed() ? 'fixed' : 'absolute',
                top: itemTop,
                bottom: (subNav.outerHeight(true) + itemTop > vwHeight) ? 0 : 'auto'
            });

        subNav.on('mouseleave', function() {
            toggleTouchItem($listItem);
            subNav.remove();
        });

        return subNav;
    }

    function removeFloatingNav() {
        $('.sidebar-subnav.nav-floating').remove();
        $('.sidebar-backdrop').remove();
        $('.sidebar li.open').removeClass('open');
    }

    function isTouch() {
        return $html.hasClass('touch');
    }

    function isSidebarCollapsed() {
        return $body.hasClass('aside-collapsed') || $body.hasClass('aside-collapsed-text');
    }

    function isSidebarToggled() {
        return $body.hasClass('aside-toggled');
    }

    function isMobile() {
        return document.body.clientWidth < APP_MEDIAQUERY.tablet;
    }

    function isFixed() {
        return $body.hasClass('layout-fixed');
    }

    function useAsideHover() {
        return $body.hasClass('aside-hover');
    }

})();
// SLIMSCROLL
// -----------------------------------

(function() {
    'use strict';

    $(initSlimsSroll);

    function initSlimsSroll() {

        if (!$.fn || !$.fn.slimScroll) return;

        $('[data-scrollable]').each(function() {

            var element = $(this),
                defaultHeight = 250;

            element.slimScroll({
                height: (element.data('height') || defaultHeight)
            });

        });
    }

})();
// Table Check All
// -----------------------------------

(function() {
    'use strict';

    $(initTableCheckAll);

    function initTableCheckAll() {

        $('[data-check-all]').on('change', function() {
            var $this = $(this),
                index = $this.index() + 1,
                checkbox = $this.find('input[type="checkbox"]'),
                table = $this.parents('table');
            // Make sure to affect only the correct checkbox column
            table.find('tbody > tr > td:nth-child(' + index + ') input[type="checkbox"]')
                .prop('checked', checkbox[0].checked);

        });

    }

})();
// TOGGLE STATE
// -----------------------------------

(function() {
    'use strict';

    $(initToggleState);

    function initToggleState() {

        var $body = $('body');
        var toggle = new StateToggler();

        $('[data-toggle-state]')
            .on('click', function(e) {
                // e.preventDefault();
                e.stopPropagation();
                var element = $(this),
                    classname = element.data('toggleState'),
                    target = element.data('target'),
                    noPersist = (element.attr('data-no-persist') !== undefined);

                // Specify a target selector to toggle classname
                // use body by default
                var $target = target ? $(target) : $body;

                if (classname) {
                    if ($target.hasClass(classname)) {
                        $target.removeClass(classname);
                        if (!noPersist)
                            toggle.removeState(classname);
                    } else {
                        $target.addClass(classname);
                        if (!noPersist)
                            toggle.addState(classname);
                    }

                }

                // some elements may need this when toggled class change the content size
                if (typeof(Event) === 'function') { // modern browsers
                    window.dispatchEvent(new Event('resize'));
                } else { // old browsers and IE
                    var resizeEvent = window.document.createEvent('UIEvents');
                    resizeEvent.initUIEvent('resize', true, false, window, 0);
                    window.dispatchEvent(resizeEvent);
                }
            });

    }

    // Handle states to/from localstorage
    var StateToggler = function() {

        var STORAGE_KEY_NAME = 'jq-toggleState';

        /** Add a state to the browser storage to be restored later */
        this.addState = function(classname) {
            var data = Storages.localStorage.get(STORAGE_KEY_NAME);
            if (data instanceof Array) data.push(classname);
            else data = [classname];
            Storages.localStorage.set(STORAGE_KEY_NAME, data);
        };
        /** Remove a state from the browser storage */
        this.removeState = function(classname) {
            var data = Storages.localStorage.get(STORAGE_KEY_NAME);
            if (data) {
                var index = data.indexOf(classname);
                if (index !== -1) data.splice(index, 1);
                Storages.localStorage.set(STORAGE_KEY_NAME, data);
            }
        };
        /** Load the state string and restore the classlist */
        this.restoreState = function($elem) {
            var data = Storages.localStorage.get(STORAGE_KEY_NAME);
            if (data instanceof Array)
                $elem.addClass(data.join(' '));
        };
    };

    window.StateToggler = StateToggler;

})();
/**=========================================================
 * Module: trigger-resize.js
 * Triggers a window resize event from any element
 =========================================================*/

(function() {
    'use strict';

    $(initTriggerResize);

    function initTriggerResize() {
        var element = $('[data-trigger-resize]');
        var value = element.data('triggerResize')
        element.on('click', function() {
            setTimeout(function() {
                // all IE friendly dispatchEvent
                var evt = document.createEvent('UIEvents');
                evt.initUIEvent('resize', true, false, window, 0);
                window.dispatchEvent(evt);
                // modern dispatchEvent way
                // window.dispatchEvent(new Event('resize'));
            }, value || 300);
        });
    }

})();

// Easypie chart Loader
// -----------------------------------

(function() {
  'use strict';

  $(initEasyPieChart);

  function initEasyPieChart() {

    if (!$.fn.easyPieChart) return;

    // Usage via data attributes
    // <div class="easypie-chart" data-easypiechart data-percent="X" data-optionName="value"></div>
    $('[data-easypiechart]').each(function() {
      var $elem = $(this);
      var options = $elem.data();
      $elem.easyPieChart(options || {});
    });

    // programmatic usage
    var pieOptions1 = {
      animate: {
        duration: 800,
        enabled: true
      },
      barColor: APP_COLORS['success'],
      trackColor: false,
      scaleColor: false,
      lineWidth: 10,
      lineCap: 'circle'
    };
    $('#easypie1').easyPieChart(pieOptions1);

    var pieOptions2 = {
      animate: {
        duration: 800,
        enabled: true
      },
      barColor: APP_COLORS['warning'],
      trackColor: false,
      scaleColor: false,
      lineWidth: 4,
      lineCap: 'circle'
    };
    $('#easypie2').easyPieChart(pieOptions2);

    var pieOptions3 = {
      animate: {
        duration: 800,
        enabled: true
      },
      barColor: APP_COLORS['danger'],
      trackColor: false,
      scaleColor: APP_COLORS['gray'],
      lineWidth: 15,
      lineCap: 'circle'
    };
    $('#easypie3').easyPieChart(pieOptions3);

    var pieOptions4 = {
      animate: {
        duration: 800,
        enabled: true
      },
      barColor: APP_COLORS['danger'],
      trackColor: APP_COLORS['yellow'],
      scaleColor: APP_COLORS['gray-dark'],
      lineWidth: 15,
      lineCap: 'circle'
    };
    $('#easypie4').easyPieChart(pieOptions4);

  }

})();
// CHART SPLINE
// -----------------------------------
(function() {
  'use strict';



  $(initFlotSpline);

  function initFlotSpline() {

    var data = [{
      "label": "Uniques",
      "color": "#768294",
      "data": [
        ["Mar", 70],
        ["Apr", 85],
        ["May", 59],
        ["Jun", 93],
        ["Jul", 66],
        ["Aug", 86],
        ["Sep", 60]
      ]
    }, {
      "label": "Recurrent",
      "color": "#1f92fe",
      "data": [
        ["Mar", 21],
        ["Apr", 12],
        ["May", 27],
        ["Jun", 24],
        ["Jul", 16],
        ["Aug", 39],
        ["Sep", 15]
      ]
    }];

    var datav2 = [{
      "label": "Hours",
      "color": "#23b7e5",
      "data": [
        ["Jan", 70],
        ["Feb", 20],
        ["Mar", 70],
        ["Apr", 85],
        ["May", 59],
        ["Jun", 93],
        ["Jul", 66],
        ["Aug", 86],
        ["Sep", 60],
        ["Oct", 60],
        ["Nov", 12],
        ["Dec", 50]
      ]
    }, {
      "label": "Commits",
      "color": "#7266ba",
      "data": [
        ["Jan", 20],
        ["Feb", 70],
        ["Mar", 30],
        ["Apr", 50],
        ["May", 85],
        ["Jun", 43],
        ["Jul", 96],
        ["Aug", 36],
        ["Sep", 80],
        ["Oct", 10],
        ["Nov", 72],
        ["Dec", 31]
      ]
    }];

    var datav3 = [{
      "label": "Home",
      "color": "#1ba3cd",
      "data": [
        ["1", 38],
        ["2", 40],
        ["3", 42],
        ["4", 48],
        ["5", 50],
        ["6", 70],
        ["7", 145],
        ["8", 70],
        ["9", 59],
        ["10", 48],
        ["11", 38],
        ["12", 29],
        ["13", 30],
        ["14", 22],
        ["15", 28]
      ]
    }, {
      "label": "Overall",
      "color": "#3a3f51",
      "data": [
        ["1", 16],
        ["2", 18],
        ["3", 17],
        ["4", 16],
        ["5", 30],
        ["6", 110],
        ["7", 19],
        ["8", 18],
        ["9", 110],
        ["10", 19],
        ["11", 16],
        ["12", 10],
        ["13", 20],
        ["14", 10],
        ["15", 20]
      ]
    }];

    var options = {
      series: {
        lines: {
          show: false
        },
        points: {
          show: true,
          radius: 4
        },
        splines: {
          show: true,
          tension: 0.4,
          lineWidth: 1,
          fill: 0.5
        }
      },
      grid: {
        borderColor: '#eee',
        borderWidth: 1,
        hoverable: true,
        backgroundColor: '#fcfcfc'
      },
      tooltip: true,
      tooltipOpts: {
        content: function(label, x, y) { return x + ' : ' + y; }
      },
      xaxis: {
        tickColor: '#fcfcfc',
        mode: 'categories'
      },
      yaxis: {
        min: 0,
        max: 150, // optional: use it for a clear represetation
        tickColor: '#eee',
        //position: 'right' or 'left',
        tickFormatter: function(v) {
          return v /* + ' visitors'*/ ;
        }
      },
      shadowSize: 0
    };

    var chart = $('.chart-spline');
    if (chart.length)
      $.plot(chart, data, options);

    var chartv2 = $('.chart-splinev2');
    if (chartv2.length)
      $.plot(chartv2, datav2, options);

    var chartv3 = $('.chart-splinev3');
    if (chartv3.length)
      $.plot(chartv3, datav3, options);

  }

})();

// CHART AREA
// -----------------------------------
(function() {
  'use strict';


  $(initFlotArea)

  function initFlotArea() {

    var data = [{
      "label": "Uniques",
      "color": "#aad874",
      "data": [
        ["Mar", 50],
        ["Apr", 84],
        ["May", 52],
        ["Jun", 88],
        ["Jul", 69],
        ["Aug", 92],
        ["Sep", 58]
      ]
    }, {
      "label": "Recurrent",
      "color": "#7dc7df",
      "data": [
        ["Mar", 13],
        ["Apr", 44],
        ["May", 44],
        ["Jun", 27],
        ["Jul", 38],
        ["Aug", 11],
        ["Sep", 39]
      ]
    }];

    var options = {
      series: {
        lines: {
          show: true,
          fill: 0.8
        },
        points: {
          show: true,
          radius: 4
        }
      },
      grid: {
        borderColor: '#eee',
        borderWidth: 1,
        hoverable: true,
        backgroundColor: '#fcfcfc'
      },
      tooltip: true,
      tooltipOpts: {
        content: function(label, x, y) { return x + ' : ' + y; }
      },
      xaxis: {
        tickColor: '#fcfcfc',
        mode: 'categories'
      },
      yaxis: {
        min: 0,
        tickColor: '#eee',
        // position: 'right' or 'left'
        tickFormatter: function(v) {
          return v + ' visitors';
        }
      },
      shadowSize: 0
    };

    var chart = $('.chart-area');
    if (chart.length)
      $.plot(chart, data, options);

  }

})();

// CHART BAR
// -----------------------------------
(function() {
  'use strict';


  $(initFlotBar)

  function initFlotBar() {

    var data = [{
      "label": "Sales",
      "color": "#9cd159",
      "data": [
        ["Jan", 27],
        ["Feb", 82],
        ["Mar", 56],
        ["Apr", 14],
        ["May", 28],
        ["Jun", 77],
        ["Jul", 23],
        ["Aug", 49],
        ["Sep", 81],
        ["Oct", 20]
      ]
    }];

    var options = {
      series: {
        bars: {
          align: 'center',
          lineWidth: 0,
          show: true,
          barWidth: 0.6,
          fill: 0.9
        }
      },
      grid: {
        borderColor: '#eee',
        borderWidth: 1,
        hoverable: true,
        backgroundColor: '#fcfcfc'
      },
      tooltip: true,
      tooltipOpts: {
        content: function(label, x, y) { return x + ' : ' + y; }
      },
      xaxis: {
        tickColor: '#fcfcfc',
        mode: 'categories'
      },
      yaxis: {
        // position: 'right' or 'left'
        tickColor: '#eee'
      },
      shadowSize: 0
    };

    var chart = $('.chart-bar');
    if (chart.length)
      $.plot(chart, data, options);

  }

})();


// CHART BAR STACKED
// -----------------------------------
(function() {
  'use strict';


  $(initFlotBarStacked);

  function initFlotBarStacked() {

    var data = [{
      "label": "Tweets",
      "color": "#51bff2",
      "data": [
        ["Jan", 56],
        ["Feb", 81],
        ["Mar", 97],
        ["Apr", 44],
        ["May", 24],
        ["Jun", 85],
        ["Jul", 94],
        ["Aug", 78],
        ["Sep", 52],
        ["Oct", 17],
        ["Nov", 90],
        ["Dec", 62]
      ]
    }, {
      "label": "Likes",
      "color": "#4a8ef1",
      "data": [
        ["Jan", 69],
        ["Feb", 135],
        ["Mar", 14],
        ["Apr", 100],
        ["May", 100],
        ["Jun", 62],
        ["Jul", 115],
        ["Aug", 22],
        ["Sep", 104],
        ["Oct", 132],
        ["Nov", 72],
        ["Dec", 61]
      ]
    }, {
      "label": "+1",
      "color": "#f0693a",
      "data": [
        ["Jan", 29],
        ["Feb", 36],
        ["Mar", 47],
        ["Apr", 21],
        ["May", 5],
        ["Jun", 49],
        ["Jul", 37],
        ["Aug", 44],
        ["Sep", 28],
        ["Oct", 9],
        ["Nov", 12],
        ["Dec", 35]
      ]
    }];

    var datav2 = [{
      "label": "Pending",
      "color": "#9289ca",
      "data": [
        ["Pj1", 86],
        ["Pj2", 136],
        ["Pj3", 97],
        ["Pj4", 110],
        ["Pj5", 62],
        ["Pj6", 85],
        ["Pj7", 115],
        ["Pj8", 78],
        ["Pj9", 104],
        ["Pj10", 82],
        ["Pj11", 97],
        ["Pj12", 110],
        ["Pj13", 62]
      ]
    }, {
      "label": "Assigned",
      "color": "#7266ba",
      "data": [
        ["Pj1", 49],
        ["Pj2", 81],
        ["Pj3", 47],
        ["Pj4", 44],
        ["Pj5", 100],
        ["Pj6", 49],
        ["Pj7", 94],
        ["Pj8", 44],
        ["Pj9", 52],
        ["Pj10", 17],
        ["Pj11", 47],
        ["Pj12", 44],
        ["Pj13", 100]
      ]
    }, {
      "label": "Completed",
      "color": "#564aa3",
      "data": [
        ["Pj1", 29],
        ["Pj2", 56],
        ["Pj3", 14],
        ["Pj4", 21],
        ["Pj5", 5],
        ["Pj6", 24],
        ["Pj7", 37],
        ["Pj8", 22],
        ["Pj9", 28],
        ["Pj10", 9],
        ["Pj11", 14],
        ["Pj12", 21],
        ["Pj13", 5]
      ]
    }];

    var options = {
      series: {
        stack: true,
        bars: {
          align: 'center',
          lineWidth: 0,
          show: true,
          barWidth: 0.6,
          fill: 0.9
        }
      },
      grid: {
        borderColor: '#eee',
        borderWidth: 1,
        hoverable: true,
        backgroundColor: '#fcfcfc'
      },
      tooltip: true,
      tooltipOpts: {
        content: function(label, x, y) { return x + ' : ' + y; }
      },
      xaxis: {
        tickColor: '#fcfcfc',
        mode: 'categories'
      },
      yaxis: {
        // position: 'right' or 'left'
        tickColor: '#eee'
      },
      shadowSize: 0
    };

    var chart = $('.chart-bar-stacked');
    if (chart.length)
      $.plot(chart, data, options);

    var chartv2 = $('.chart-bar-stackedv2');
    if (chartv2.length)
      $.plot(chartv2, datav2, options);

  }

})();

// CHART DONUT
// -----------------------------------
(function() {
  'use strict';


  $(initFlotDonut);

  function initFlotDonut() {

    var data = [{
      "color": "#39C558",
      "data": 60,
      "label": "Coffee"
    }, {
      "color": "#00b4ff",
      "data": 90,
      "label": "CSS"
    }, {
      "color": "#FFBE41",
      "data": 50,
      "label": "LESS"
    }, {
      "color": "#ff3e43",
      "data": 80,
      "label": "Jade"
    }, {
      "color": "#937fc7",
      "data": 116,
      "label": "AngularJS"
    }];

    var options = {
      series: {
        pie: {
          show: true,
          innerRadius: 0.5 // This makes the donut shape
        }
      }
    };

    var chart = $('.chart-donut');
    if (chart.length)
      $.plot(chart, data, options);

  }

})();

// CHART LINE
// -----------------------------------
(function() {
  'use strict';


  $(initFlotLine)

  function initFlotLine() {

    var data = [{
      "label": "Complete",
      "color": "#5ab1ef",
      "data": [
        ["Jan", 188],
        ["Feb", 183],
        ["Mar", 185],
        ["Apr", 199],
        ["May", 190],
        ["Jun", 194],
        ["Jul", 194],
        ["Aug", 184],
        ["Sep", 74]
      ]
    }, {
      "label": "In Progress",
      "color": "#f5994e",
      "data": [
        ["Jan", 153],
        ["Feb", 116],
        ["Mar", 136],
        ["Apr", 119],
        ["May", 148],
        ["Jun", 133],
        ["Jul", 118],
        ["Aug", 161],
        ["Sep", 59]
      ]
    }, {
      "label": "Cancelled",
      "color": "#d87a80",
      "data": [
        ["Jan", 111],
        ["Feb", 97],
        ["Mar", 93],
        ["Apr", 110],
        ["May", 102],
        ["Jun", 93],
        ["Jul", 92],
        ["Aug", 92],
        ["Sep", 44]
      ]
    }];

    var options = {
      series: {
        lines: {
          show: true,
          fill: 0.01
        },
        points: {
          show: true,
          radius: 4
        }
      },
      grid: {
        borderColor: '#eee',
        borderWidth: 1,
        hoverable: true,
        backgroundColor: '#fcfcfc'
      },
      tooltip: true,
      tooltipOpts: {
        content: function(label, x, y) { return x + ' : ' + y; }
      },
      xaxis: {
        tickColor: '#eee',
        mode: 'categories'
      },
      yaxis: {
        // position: 'right' or 'left'
        tickColor: '#eee'
      },
      shadowSize: 0
    };

    var chart = $('.chart-line');
    if (chart.length)
      $.plot(chart, data, options);

  }

})();


// CHART PIE
// -----------------------------------
(function() {
  'use strict';


  $(initFlotPie);

  function initFlotPie() {

    var data = [{
      "label": "jQuery",
      "color": "#4acab4",
      "data": 30
    }, {
      "label": "CSS",
      "color": "#ffea88",
      "data": 40
    }, {
      "label": "LESS",
      "color": "#ff8153",
      "data": 90
    }, {
      "label": "SASS",
      "color": "#878bb6",
      "data": 75
    }, {
      "label": "Jade",
      "color": "#b2d767",
      "data": 120
    }];

    var options = {
      series: {
        pie: {
          show: true,
          innerRadius: 0,
          label: {
            show: true,
            radius: 0.8,
            formatter: function(label, series) {
              return '<div class="flot-pie-label">' +
                //label + ' : ' +
                Math.round(series.percent) +
                '%</div>';
            },
            background: {
              opacity: 0.8,
              color: '#222'
            }
          }
        }
      }
    };

    // var chart = $('.chart-pie');
    // if (chart.length)
    //   $.plot(chart, data, options);

  }

})();


// Custom Code
// -----------------------------------

(function() {
    'use strict';

    $(initCustom);

    function initCustom() {
        // custom code
      console.log('custom code from js');
    }

})();

function initCustomHello() {
  // custom code
  console.log('custom a code from js');
}
