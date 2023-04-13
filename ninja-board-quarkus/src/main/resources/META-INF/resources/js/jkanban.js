(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
/**
 * jKanban
 * Vanilla Javascript plugin for manage kanban boards
 *
 * @site: http://www.riccardotartaglia.it/jkanban/
 * @author: Riccardo Tartaglia
 */

//Require dragula
var dragula = require('dragula');

(function () {

    this.jKanban = function () {
        var self = this;
        this._disallowedItemProperties = ['id', 'title', 'click', 'drag', 'dragend', 'drop', 'order'];
        this.element = '';
        this.container = '';
        this.boardContainer = [];
        this.dragula = dragula;
        this.drake = '';
        this.drakeBoard = '';
        this.addItemButton = false;
        this.buttonContent = '+';
        defaults = {
            element: '',
            gutter: '15px',
            widthBoard: '250px',
            responsive: '700',
            responsivePercentage: false,
            boards: [],
            dragBoards: true,
            addItemButton: false,
            buttonContent: '+',
            dragEl: function (el, source) {
            },
            dragendEl: function (el) {
            },
            dropEl: function (el, target, source, sibling) {
            },
            dragBoard: function (el, source) {
            },
            dragendBoard: function (el) {
            },
            dropBoard: function (el, target, source, sibling) {
            },
            click: function (el) {
            },
            buttonClick: function (el, boardId) {
            }
        };

        if (arguments[0] && typeof arguments[0] === "object") {
            this.options = __extendDefaults(defaults, arguments[0]);
        }

        this.init = function () {
            //set initial boards
            __setBoard();
            //set drag with dragula
            if (window.innerWidth > self.options.responsive) {

                //Init Drag Board
                self.drakeBoard = self.dragula([self.container], {
                    moves: function (el, source, handle, sibling) {
                        if (!self.options.dragBoards) return false;
                        return (handle.classList.contains('kanban-board-header') || handle.classList.contains('kanban-title-board'));
                    },
                    accepts: function (el, target, source, sibling) {
                        return target.classList.contains('kanban-container');
                    },
                    revertOnSpill: true,
                    direction: 'horizontal',
                })
                    .on('drag', function (el, source) {
                        el.classList.add('is-moving');
                        self.options.dragBoard(el, source);
                        if (typeof(el.dragfn) === 'function')
                            el.dragfn(el, source);
                    })
                    .on('dragend', function (el) {
                        __updateBoardsOrder();
                        el.classList.remove('is-moving');
                        self.options.dragendBoard(el);
                        if (typeof(el.dragendfn) === 'function')
                            el.dragendfn(el);
                    })
                    .on('drop', function (el, target, source, sibling) {
                        el.classList.remove('is-moving');
                        self.options.dropBoard(el, target, source, sibling);
                        if (typeof(el.dropfn) === 'function')
                            el.dropfn(el, target, source, sibling);
                    });

                //Init Drag Item
                self.drake = self.dragula(self.boardContainer, function () {
                    revertOnSpill: true
                })
                    .on('cancel', function(el, container, source) {
                        self.enableAllBoards();
                    })
                    .on('drag', function (el, source) {
                        var elClass = el.getAttribute("class");
                        if (elClass !== "" && elClass.indexOf("not-draggable") > -1) {
                            self.drake.cancel(true);
                            return;
                        }

                        el.classList.add('is-moving');
                        var boardJSON = __findBoardJSON(source.parentNode.dataset.id);
                        if (boardJSON.dragTo !== undefined) {
                            self.options.boards.map(function (board) {
                                if (boardJSON.dragTo.indexOf(board.id) === -1 && board.id !== source.parentNode.dataset.id) {
                                    self.findBoard(board.id).classList.add('disabled-board');
                                }
                            })
                        }

                        self.options.dragEl(el, source);
                        if (el !== null && typeof(el.dragfn) === 'function')
                            el.dragfn(el, source);
                    })
                    .on('dragend', function (el) {
                        self.options.dragendEl(el);
                        if (el !== null && typeof(el.dragendfn) === 'function')
                            el.dragendfn(el);
                    })
                    .on('drop', function (el, target, source, sibling) {
                        self.enableAllBoards();

                        var boardJSON = __findBoardJSON(source.parentNode.dataset.id);
                        if (boardJSON.dragTo !== undefined) {
                            if (boardJSON.dragTo.indexOf(target.parentNode.dataset.id) === -1 && target.parentNode.dataset.id !== source.parentNode.dataset.id) {
                                self.drake.cancel(true)
                            }
                        }
                        if (el !== null) {
                            self.options.dropEl(el, target, source, sibling);
                            el.classList.remove('is-moving');
                            if (typeof(el.dropfn) === 'function')
                                el.dropfn(el, target, source, sibling);
                        }
                    })
            }
        };

        this.enableAllBoards = function() {
            var allB = document.querySelectorAll('.kanban-board');
            if (allB.length > 0 && allB !== undefined) {
                for (var i = 0; i < allB.length; i++) {
                    allB[i].classList.remove('disabled-board');
                }
            }
        };

        this.addElement = function (boardID, element) {
            var board = self.element.querySelector('[data-id="' + boardID + '"] .kanban-drag');
            var nodeItem = document.createElement('div');
            nodeItem.classList.add('kanban-item');
            if (element.id) {
              nodeItem.setAttribute('data-eid', element.id)
            }
            //nodeItem.innerHTML = element.title;
            
            var nodeItemTitle = document.createElement('div');
            nodeItemTitle.classList.add('card-item');
            nodeItemTitle.innerHTML=cardDisplay(board.id, nodeItem);
            nodeItem.appendChild(nodeItemTitle);
            
            //add function
            nodeItem.clickfn = element.click;
            nodeItem.dragfn = element.drag;
            nodeItem.dragendfn = element.dragend;
            nodeItem.dropfn = element.drop;
            __appendCustomProperties(nodeItem, element);
            __onclickHandler(nodeItem);
            board.appendChild(nodeItem);
            
            
            return self;
        };

        this.addForm = function (boardID, formItem) {
            var board = self.element.querySelector('[data-id="' + boardID + '"] .kanban-drag');
            var _attribute = formItem.getAttribute("class");
            formItem.setAttribute("class", _attribute + " not-draggable");
            board.appendChild(formItem);
            return self;
        };

        this.addBoards = function (boards) {
            if (self.options.responsivePercentage) {
                self.container.style.width = '100%';
                self.options.gutter = '1%';
                if (window.innerWidth > self.options.responsive) {
                    var boardWidth = (100 - boards.length * 2) / boards.length;
                } else {
                    var boardWidth = 100 - (boards.length * 2);
                }
            } else {
                var boardWidth = self.options.widthBoard;
            }
            var addButton = self.options.addItemButton;
            var buttonContent = self.options.buttonContent;


            //for on all the boards
            for (var boardkey in boards) {
                // single board
                var board = boards[boardkey];
                self.options.boards.push(board);

                if (!self.options.responsivePercentage) {
                    //add width to container
                    if (self.container.style.width === '') {
                        self.container.style.width = parseInt(boardWidth) + (parseInt(self.options.gutter) * 2) + 'px';
                    } else {
                        self.container.style.width = parseInt(self.container.style.width) + parseInt(boardWidth) + (parseInt(self.options.gutter) * 2) + 'px';
                    }
                }
                //create node
                var boardNode = document.createElement('div');
                boardNode.dataset.id = board.id;
                boardNode.dataset.order = self.container.childNodes.length + 1;
                boardNode.classList.add('kanban-board');
                //set style
                if (self.options.responsivePercentage) {
                    boardNode.style.width = boardWidth + '%';
                } else {
                    boardNode.style.width = boardWidth;
                }
                boardNode.style.marginLeft = self.options.gutter;
                boardNode.style.marginRight = self.options.gutter;
                // header board
                var headerBoard = document.createElement('header');
                if (board.class !== '' && board.class !== undefined)
                    var allClasses = board.class.split(",");
                else allClasses = [];
                headerBoard.classList.add('kanban-board-header');
                allClasses.map(function (value) {
                    headerBoard.classList.add(value);
                });
                headerBoard.innerHTML = '<div class="kanban-title-board">' + board.title + '</div>';
                // if add button is true, add button to the board
                if (addButton) {
                    var btn = document.createElement("BUTTON");
                    var t = document.createTextNode(buttonContent);
                    btn.setAttribute("class", "kanban-title-button btn btn-default btn-xs");
                    btn.appendChild(t);
                    //var buttonHtml = '<button class="kanban-title-button btn btn-default btn-xs">'+buttonContent+'</button>'
                    headerBoard.appendChild(btn);
                    __onButtonClickHandler(btn, board.id);
                }
                //content board
                var contentBoard = document.createElement('main');
                contentBoard.classList.add('kanban-drag');
                if (board.bodyClass !== '' && board.bodyClass !== undefined)
                    var bodyClasses = board.bodyClass.split(",");
                else bodyClasses = [];
                bodyClasses.map(function (value) {
                    contentBoard.classList.add(value);
                });
                //add drag to array for dragula
                self.boardContainer.push(contentBoard);
                for (var itemkey in board.item) {
                    //create item
                    var itemKanban = board.item[itemkey];
                    var nodeItem = document.createElement('div');
                    nodeItem.classList.add('kanban-item');
                    nodeItem.dataset.eid = itemKanban.id;
                    
                    
                    var nodeItemTitle = document.createElement('div');
                    nodeItemTitle.classList.add('card-item');
                    
                    //add function
                    nodeItemTitle.clickfn = itemKanban.click;
                    nodeItem.dragfn = itemKanban.drag;
                    nodeItem.dragendfn = itemKanban.dragend;
                    nodeItem.dropfn = itemKanban.drop;
                    __appendCustomProperties(nodeItem, itemKanban);
                    //add click handler of item
                    __onclickHandler(nodeItemTitle, nodeItem);
                    
                    //console.log(JSON.stringify(itemKanban));
                    
                    // Display card text
                    nodeItemTitle.innerHTML=cardDisplay(board.id, nodeItem, itemKanban);
                    
                    if (self.options.labels){
                      
                      var labels=nodeItemTitle.querySelectorAll(".btnLabelDelete");
                      for (i=0; i<labels.length; i++)
                        __onClickHandler2(labels[i], nodeItem, self.options.onLabelDelete, function(caller){
                        	caller.parentElement.remove();
                        });
                      
                      var labels=nodeItemTitle.querySelectorAll(".btnLabelNew");
                      for (i=0; i<labels.length; i++){
                        __onBlurHandler2(labels[i], nodeItem, self.options.onLabelNew, function (newLabelTextBox, taskId, label){
                          var pill="<span class='label-pill ng-scope label-pill-green'>"+label+" <button class='btnLabelDelete' data-id='"+taskId+"' data-label='"+label+"'>x</button></span>";
                          
                          var labelsWrapper=newLabelTextBox.closest(".labels").querySelector(".labelswrapper"); //look up till we hit .labels, then down to the .labelswrapper span
                          labelsWrapper.innerHTML+=pill;
                          newLabelTextBox.value=""; // clear "new label" text box
                          
                          __attachOnLabelDeleteHandlers(labelsWrapper, nodeItem);
                        });
                      }
                      
                      __attachOnLabelDeleteHandlers(nodeItemTitle.querySelector(".labelswrapper"), nodeItem);
                    }
                    
                    
                    if (self.options.deleteCards)
                      __ondeleteHandler(nodeItemTitle.querySelector(".btnDeleteCard"), nodeItem, self.options.onDelete);
                    
                    __onBlurHandler2(nodeItemTitle.querySelector(".title"), nodeItem, self.options.onUpdate, function (newLabelTextBox, taskId, label){
                    	// this is only called on successful update of title, so update the title to the new value?
                    });
                    
                    nodeItem.appendChild(nodeItemTitle);
                    contentBoard.appendChild(nodeItem);
                }
                //footer board
                var footerBoard = document.createElement('footer');
                //board assembly
                boardNode.appendChild(headerBoard);
                boardNode.appendChild(contentBoard);
                boardNode.appendChild(footerBoard);
                //board add
                self.container.appendChild(boardNode);
            }
            return self;
        }

        this.findBoard = function (id) {
            var el = self.element.querySelector('[data-id="' + id + '"]');
            return el;
        }

        this.findElement = function (id) {
            var el = self.element.querySelector('[data-eid="' + id + '"]');
            return el;
        }

        this.getBoardElements = function (id) {
            var board = self.element.querySelector('[data-id="' + id + '"] .kanban-drag');
            return (board.childNodes);
        }

        this.removeElement = function (el) {
            if (typeof(el) === 'string')
                el = self.element.querySelector('[data-eid="' + el + '"]');
            if (el !== null) {
                el.remove();
            }
            return self;
        };

        this.removeBoard = function (board) {
            if (typeof(board) === 'string')
                board = self.element.querySelector('[data-id="' + board + '"]');
            if (board !== null) {
                board.remove();
            }
            return self;
        }

        // board button on click function
        this.onButtonClick = function (el) {

        }

        //PRIVATE FUNCTION
        function __extendDefaults(source, properties) {
            var property;
            for (property in properties) {
                if (properties.hasOwnProperty(property)) {
                    source[property] = properties[property];
                }
            }
            return source;
        }

        function __setBoard() {
            self.element = document.querySelector(self.options.element);
            //create container
            var boardContainer = document.createElement('div');
            boardContainer.classList.add('kanban-container');
            
            // add menu container
            //var ctxMenu = document.createElement('div');
            //ctxMenu.classList.add('ctx-menu');
            //boardContainer.appendChild(ctxMenu);
            
            self.container = boardContainer;
            //add boards
            self.addBoards(self.options.boards);
            //appends to container
            self.element.appendChild(self.container);
        };
        
        function __attachOnLabelDeleteHandlers(labelsWrapper, nodeItem){
          var deleteButtons=labelsWrapper.querySelectorAll(".btnLabelDelete");
          for (i=0; i<deleteButtons.length; i++)
            __onClickHandler2(deleteButtons[i], nodeItem, self.options.onLabelDelete, function(deleteLabelSuccess){
            	deleteLabelSuccess.closest(".label-pill").remove();
            });
        }
        
        function __onClickHandler2(target, nodeItem, clickfn, onSuccess) {
        	target.addEventListener('click', function (e) {
                e.preventDefault();
                clickfn(target, nodeItem, onSuccess);
                if (typeof(this.clickfn) === 'function')
                    this.clickfn(nodeItem);
            });
        }

        function __onBlurHandler2(target, nodeItem, blurfn, onSuccess) {
        	target.addEventListener('blur', function (e) {
                e.preventDefault();
                blurfn(target, nodeItem, onSuccess);
                //if (typeof(this.blurfn) === 'function')
                //    this.blurfn(nodeItem);
            });
        	target.addEventListener('keypress', function(e){
        		var key = e.which || e.keyCode;
        		if (key === 13)
        			target.blur();
        	});
        }
        
        function __onupdateHandler(target, nodeItem, onUpdateFn) {
        	target.addEventListener('blur', function (e) {
                e.preventDefault();
                onUpdateFn(target, nodeItem);
                if (typeof(this.blurfn) === 'function')
                    this.blurfn(nodeItem);
            });
        }

        function __ondeleteHandler(target, nodeItem, clickfn) {
        	target.addEventListener('click', function (e) {
                e.preventDefault();
                self.options.onDelete(nodeItem);
                if (typeof(this.clickfn) === 'function')
                    this.clickfn(nodeItem);
            });
        }

        function __onclickHandler(target, nodeItem, clickfn) {
        	target.addEventListener('click', function (e) {
                e.preventDefault();
                self.options.click(nodeItem);
                if (typeof(this.clickfn) === 'function')
                    this.clickfn(nodeItem);
            });
        }

        function __onButtonClickHandler(nodeItem, boardId) {
            nodeItem.addEventListener('click', function (e) {
                e.preventDefault();
                self.options.buttonClick(this, boardId);
                // if(typeof(this.clickfn) === 'function')
                //     this.clickfn(this);
            });
        }

        function __findBoardJSON(id) {
            var el = []
            self.options.boards.map(function (board) {
                if (board.id === id) {
                    return el.push(board)
                }
            })
            return el[0]
        }

        function __appendCustomProperties(element, parentObject) {
            for (var propertyName in parentObject) {
                if (self._disallowedItemProperties.indexOf(propertyName) > -1) {
                    continue;
                }

                element.setAttribute('data-' + propertyName, parentObject[propertyName]);
            }
        }

        function __updateBoardsOrder() {
            var index = 1;
            for (var i = 0; i < self.container.childNodes.length; i++) {
                self.container.childNodes[i].dataset.order = index++;
            }
        }

        function cardDisplay(boardId, el, data){
          var result="<div class='card'>";
          result+="<div class='header'><a class='id' href='#'>"+data.id+"</a><span class='right'>";
          
          if (self.options.userAssignment)
            result+="<button class='action-btn'><i class='unassigned fa fa-user'></i></button>";
          
          result+="</span></div>";
          result+="<div class='body'>";
          
          if (undefined!=self.options.customDisplay){
            result+=self.options.customDisplay(boardId, el, data);
          }else{
            result+="<textarea class='title'>"+data.title+"</textarea>";
          }
          
          result+="</div><div class='footer clearfix'>";
          result+="<div class='labels'>";
          result+="<span class='labelswrapper'>";
          if (self.options.labels){
            if (undefined!=data.labels){
              var labelsArray=data.labels.split(",");
              for (i=0;i<labelsArray.length;i++){
                if (labelsArray[i]!="")
                  result+="<span class='label-pill ng-scope label-pill-green'>"+labelsArray[i]+" <button class='btnLabelDelete' data-id='"+data.id+"' data-label='"+labelsArray[i]+"'>x</button></span>";
              }
            }
          }
          
          result+="</span>";
          result+="<span class='label-pill ng-scope label-pill-new'><input class='btnLabelNew' onBlur='this.parentNode.classList.remove(\"open\")' onFocus='this.parentNode.classList.add(\"open\")' data-id='"+data.id+"' type='text'/></span>";
          result+="</div>";
          
          result+="<div class='actions right'>";
          
          if (self.options.comments)
            result+="<button class='action-btn'><i class='fas fas-comment-alt'></i></button>";
            
          if (self.options.contextMenu){
            result+='<div class="action-menu">';
            result+='  <button data-toggle="dropdown" class="dropdown-toggle action-btn" >';
            result+='    <i class="fa fa-ellipsis-v action-menu-icon"></i>';
            result+='  </button>';
            result+='  <ul class="dropdown-menu card-dropdown-menu">';
            
            if (self.options.deleteCards){
              result+='    <li class="dropdown-menu-item">';
              result+='      <button class="btnDeleteCard action-btn">Delete Card</button>';
              result+='    </li>';
            }
            result+='  </ul>';
            result+='</div>';
            
          }
          
          result+="</div></div>";
          result+="</div>";
        
          return result;
        }
        
        //init plugin
        this.init();
    };
    
}());


},{"dragula":9}],2:[function(require,module,exports){
module.exports = function atoa (a, n) { return Array.prototype.slice.call(a, n); }

},{}],3:[function(require,module,exports){
'use strict';

var ticky = require('ticky');

module.exports = function debounce (fn, args, ctx) {
  if (!fn) { return; }
  ticky(function run () {
    fn.apply(ctx || null, args || []);
  });
};

},{"ticky":10}],4:[function(require,module,exports){
'use strict';

var atoa = require('atoa');
var debounce = require('./debounce');

module.exports = function emitter (thing, options) {
  var opts = options || {};
  var evt = {};
  if (thing === undefined) { thing = {}; }
  thing.on = function (type, fn) {
    if (!evt[type]) {
      evt[type] = [fn];
    } else {
      evt[type].push(fn);
    }
    return thing;
  };
  thing.once = function (type, fn) {
    fn._once = true; // thing.off(fn) still works!
    thing.on(type, fn);
    return thing;
  };
  thing.off = function (type, fn) {
    var c = arguments.length;
    if (c === 1) {
      delete evt[type];
    } else if (c === 0) {
      evt = {};
    } else {
      var et = evt[type];
      if (!et) { return thing; }
      et.splice(et.indexOf(fn), 1);
    }
    return thing;
  };
  thing.emit = function () {
    var args = atoa(arguments);
    return thing.emitterSnapshot(args.shift()).apply(this, args);
  };
  thing.emitterSnapshot = function (type) {
    var et = (evt[type] || []).slice(0);
    return function () {
      var args = atoa(arguments);
      var ctx = this || thing;
      if (type === 'error' && opts.throws !== false && !et.length) { throw args.length === 1 ? args[0] : args; }
      et.forEach(function emitter (listen) {
        if (opts.async) { debounce(listen, args, ctx); } else { listen.apply(ctx, args); }
        if (listen._once) { thing.off(type, listen); }
      });
      return thing;
    };
  };
  return thing;
};

},{"./debounce":3,"atoa":2}],5:[function(require,module,exports){
(function (global){
'use strict';

var customEvent = require('custom-event');
var eventmap = require('./eventmap');
var doc = global.document;
var addEvent = addEventEasy;
var removeEvent = removeEventEasy;
var hardCache = [];

if (!global.addEventListener) {
  addEvent = addEventHard;
  removeEvent = removeEventHard;
}

module.exports = {
  add: addEvent,
  remove: removeEvent,
  fabricate: fabricateEvent
};

function addEventEasy (el, type, fn, capturing) {
  return el.addEventListener(type, fn, capturing);
}

function addEventHard (el, type, fn) {
  return el.attachEvent('on' + type, wrap(el, type, fn));
}

function removeEventEasy (el, type, fn, capturing) {
  return el.removeEventListener(type, fn, capturing);
}

function removeEventHard (el, type, fn) {
  var listener = unwrap(el, type, fn);
  if (listener) {
    return el.detachEvent('on' + type, listener);
  }
}

function fabricateEvent (el, type, model) {
  var e = eventmap.indexOf(type) === -1 ? makeCustomEvent() : makeClassicEvent();
  if (el.dispatchEvent) {
    el.dispatchEvent(e);
  } else {
    el.fireEvent('on' + type, e);
  }
  function makeClassicEvent () {
    var e;
    if (doc.createEvent) {
      e = doc.createEvent('Event');
      e.initEvent(type, true, true);
    } else if (doc.createEventObject) {
      e = doc.createEventObject();
    }
    return e;
  }
  function makeCustomEvent () {
    return new customEvent(type, { detail: model });
  }
}

function wrapperFactory (el, type, fn) {
  return function wrapper (originalEvent) {
    var e = originalEvent || global.event;
    e.target = e.target || e.srcElement;
    e.preventDefault = e.preventDefault || function preventDefault () { e.returnValue = false; };
    e.stopPropagation = e.stopPropagation || function stopPropagation () { e.cancelBubble = true; };
    e.which = e.which || e.keyCode;
    fn.call(el, e);
  };
}

function wrap (el, type, fn) {
  var wrapper = unwrap(el, type, fn) || wrapperFactory(el, type, fn);
  hardCache.push({
    wrapper: wrapper,
    element: el,
    type: type,
    fn: fn
  });
  return wrapper;
}

function unwrap (el, type, fn) {
  var i = find(el, type, fn);
  if (i) {
    var wrapper = hardCache[i].wrapper;
    hardCache.splice(i, 1); // free up a tad of memory
    return wrapper;
  }
}

function find (el, type, fn) {
  var i, item;
  for (i = 0; i < hardCache.length; i++) {
    item = hardCache[i];
    if (item.element === el && item.type === type && item.fn === fn) {
      return i;
    }
  }
}

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"./eventmap":6,"custom-event":7}],6:[function(require,module,exports){
(function (global){
'use strict';

var eventmap = [];
var eventname = '';
var ron = /^on/;

for (eventname in global) {
  if (ron.test(eventname)) {
    eventmap.push(eventname.slice(2));
  }
}

module.exports = eventmap;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],7:[function(require,module,exports){
(function (global){

var NativeCustomEvent = global.CustomEvent;

function useNative () {
  try {
    var p = new NativeCustomEvent('cat', { detail: { foo: 'bar' } });
    return  'cat' === p.type && 'bar' === p.detail.foo;
  } catch (e) {
  }
  return false;
}

/**
 * Cross-browser `CustomEvent` constructor.
 *
 * https://developer.mozilla.org/en-US/docs/Web/API/CustomEvent.CustomEvent
 *
 * @public
 */

module.exports = useNative() ? NativeCustomEvent :

// IE >= 9
'function' === typeof document.createEvent ? function CustomEvent (type, params) {
  var e = document.createEvent('CustomEvent');
  if (params) {
    e.initCustomEvent(type, params.bubbles, params.cancelable, params.detail);
  } else {
    e.initCustomEvent(type, false, false, void 0);
  }
  return e;
} :

// IE <= 8
function CustomEvent (type, params) {
  var e = document.createEventObject();
  e.type = type;
  if (params) {
    e.bubbles = Boolean(params.bubbles);
    e.cancelable = Boolean(params.cancelable);
    e.detail = params.detail;
  } else {
    e.bubbles = false;
    e.cancelable = false;
    e.detail = void 0;
  }
  return e;
}

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}],8:[function(require,module,exports){
'use strict';

var cache = {};
var start = '(?:^|\\s)';
var end = '(?:\\s|$)';

function lookupClass (className) {
  var cached = cache[className];
  if (cached) {
    cached.lastIndex = 0;
  } else {
    cache[className] = cached = new RegExp(start + className + end, 'g');
  }
  return cached;
}

function addClass (el, className) {
  var current = el.className;
  if (!current.length) {
    el.className = className;
  } else if (!lookupClass(className).test(current)) {
    el.className += ' ' + className;
  }
}

function rmClass (el, className) {
  el.className = el.className.replace(lookupClass(className), ' ').trim();
}

module.exports = {
  add: addClass,
  rm: rmClass
};

},{}],9:[function(require,module,exports){
(function (global){
'use strict';

var emitter = require('contra/emitter');
var crossvent = require('crossvent');
var classes = require('./classes');
var doc = document;
var documentElement = doc.documentElement;

function dragula (initialContainers, options) {
  var len = arguments.length;
  if (len === 1 && Array.isArray(initialContainers) === false) {
    options = initialContainers;
    initialContainers = [];
  }
  var _mirror; // mirror image
  var _source; // source container
  var _item; // item being dragged
  var _offsetX; // reference x
  var _offsetY; // reference y
  var _moveX; // reference move x
  var _moveY; // reference move y
  var _initialSibling; // reference sibling when grabbed
  var _currentSibling; // reference sibling now
  var _copy; // item used for copying
  var _renderTimer; // timer for setTimeout renderMirrorImage
  var _lastDropTarget = null; // last container item was over
  var _grabbed; // holds mousedown context until first mousemove

  var o = options || {};
  if (o.moves === void 0) { o.moves = always; }
  if (o.accepts === void 0) { o.accepts = always; }
  if (o.invalid === void 0) { o.invalid = invalidTarget; }
  if (o.containers === void 0) { o.containers = initialContainers || []; }
  if (o.isContainer === void 0) { o.isContainer = never; }
  if (o.copy === void 0) { o.copy = false; }
  if (o.copySortSource === void 0) { o.copySortSource = false; }
  if (o.revertOnSpill === void 0) { o.revertOnSpill = false; }
  if (o.removeOnSpill === void 0) { o.removeOnSpill = false; }
  if (o.direction === void 0) { o.direction = 'vertical'; }
  if (o.ignoreInputTextSelection === void 0) { o.ignoreInputTextSelection = true; }
  if (o.mirrorContainer === void 0) { o.mirrorContainer = doc.body; }

  var drake = emitter({
    containers: o.containers,
    start: manualStart,
    end: end,
    cancel: cancel,
    remove: remove,
    destroy: destroy,
    canMove: canMove,
    dragging: false
  });

  if (o.removeOnSpill === true) {
    drake.on('over', spillOver).on('out', spillOut);
  }

  events();

  return drake;

  function isContainer (el) {
    return drake.containers.indexOf(el) !== -1 || o.isContainer(el);
  }

  function events (remove) {
    var op = remove ? 'remove' : 'add';
    touchy(documentElement, op, 'mousedown', grab);
    touchy(documentElement, op, 'mouseup', release);
  }

  function eventualMovements (remove) {
    var op = remove ? 'remove' : 'add';
    touchy(documentElement, op, 'mousemove', startBecauseMouseMoved);
  }

  function movements (remove) {
    var op = remove ? 'remove' : 'add';
    crossvent[op](documentElement, 'selectstart', preventGrabbed); // IE8
    crossvent[op](documentElement, 'click', preventGrabbed);
  }

  function destroy () {
    events(true);
    release({});
  }

  function preventGrabbed (e) {
    if (_grabbed) {
      e.preventDefault();
    }
  }

  function grab (e) {
    _moveX = e.clientX;
    _moveY = e.clientY;

    var ignore = whichMouseButton(e) !== 1 || e.metaKey || e.ctrlKey;
    if (ignore) {
      return; // we only care about honest-to-god left clicks and touch events
    }
    var item = e.target;
    var context = canStart(item);
    if (!context) {
      return;
    }
    _grabbed = context;
    eventualMovements();
    if (e.type === 'mousedown') {
      if (isInput(item)) { // see also: https://github.com/bevacqua/dragula/issues/208
        item.focus(); // fixes https://github.com/bevacqua/dragula/issues/176
      } else {
        e.preventDefault(); // fixes https://github.com/bevacqua/dragula/issues/155
      }
    }
  }

  function startBecauseMouseMoved (e) {
    if (!_grabbed) {
      return;
    }
    if (whichMouseButton(e) === 0) {
      release({});
      return; // when text is selected on an input and then dragged, mouseup doesn't fire. this is our only hope
    }
    // truthy check fixes #239, equality fixes #207
    if (e.clientX !== void 0 && e.clientX === _moveX && e.clientY !== void 0 && e.clientY === _moveY) {
      return;
    }
    if (o.ignoreInputTextSelection) {
      var clientX = getCoord('clientX', e);
      var clientY = getCoord('clientY', e);
      var elementBehindCursor = doc.elementFromPoint(clientX, clientY);
      if (isInput(elementBehindCursor)) {
        return;
      }
    }

    var grabbed = _grabbed; // call to end() unsets _grabbed
    eventualMovements(true);
    movements();
    end();
    start(grabbed);

    var offset = getOffset(_item);
    _offsetX = getCoord('pageX', e) - offset.left;
    _offsetY = getCoord('pageY', e) - offset.top;

    classes.add(_copy || _item, 'gu-transit');
    renderMirrorImage();
    drag(e);
  }

  function canStart (item) {
    if (drake.dragging && _mirror) {
      return;
    }
    if (isContainer(item)) {
      return; // don't drag container itself
    }
    var handle = item;
    while (getParent(item) && isContainer(getParent(item)) === false) {
      if (o.invalid(item, handle)) {
        return;
      }
      item = getParent(item); // drag target should be a top element
      if (!item) {
        return;
      }
    }
    var source = getParent(item);
    if (!source) {
      return;
    }
    if (o.invalid(item, handle)) {
      return;
    }

    var movable = o.moves(item, source, handle, nextEl(item));
    if (!movable) {
      return;
    }

    return {
      item: item,
      source: source
    };
  }

  function canMove (item) {
    return !!canStart(item);
  }

  function manualStart (item) {
    var context = canStart(item);
    if (context) {
      start(context);
    }
  }

  function start (context) {
    if (isCopy(context.item, context.source)) {
      _copy = context.item.cloneNode(true);
      drake.emit('cloned', _copy, context.item, 'copy');
    }

    _source = context.source;
    _item = context.item;
    _initialSibling = _currentSibling = nextEl(context.item);

    drake.dragging = true;
    drake.emit('drag', _item, _source);
  }

  function invalidTarget () {
    return false;
  }

  function end () {
    if (!drake.dragging) {
      return;
    }
    var item = _copy || _item;
    drop(item, getParent(item));
  }

  function ungrab () {
    _grabbed = false;
    eventualMovements(true);
    movements(true);
  }

  function release (e) {
    ungrab();

    if (!drake.dragging) {
      return;
    }
    var item = _copy || _item;
    var clientX = getCoord('clientX', e);
    var clientY = getCoord('clientY', e);
    var elementBehindCursor = getElementBehindPoint(_mirror, clientX, clientY);
    var dropTarget = findDropTarget(elementBehindCursor, clientX, clientY);
    if (dropTarget && ((_copy && o.copySortSource) || (!_copy || dropTarget !== _source))) {
      drop(item, dropTarget);
    } else if (o.removeOnSpill) {
      remove();
    } else {
      cancel();
    }
  }

  function drop (item, target) {
    var parent = getParent(item);
    if (_copy && o.copySortSource && target === _source) {
      parent.removeChild(_item);
    }
    if (isInitialPlacement(target)) {
      drake.emit('cancel', item, _source, _source);
    } else {
      drake.emit('drop', item, target, _source, _currentSibling);
    }
    cleanup();
  }

  function remove () {
    if (!drake.dragging) {
      return;
    }
    var item = _copy || _item;
    var parent = getParent(item);
    if (parent) {
      parent.removeChild(item);
    }
    drake.emit(_copy ? 'cancel' : 'remove', item, parent, _source);
    cleanup();
  }

  function cancel (revert) {
    if (!drake.dragging) {
      return;
    }
    var reverts = arguments.length > 0 ? revert : o.revertOnSpill;
    var item = _copy || _item;
    var parent = getParent(item);
    var initial = isInitialPlacement(parent);
    if (initial === false && reverts) {
      if (_copy) {
        if (parent) {
          parent.removeChild(_copy);
        }
      } else {
        _source.insertBefore(item, _initialSibling);
      }
    }
    if (initial || reverts) {
      drake.emit('cancel', item, _source, _source);
    } else {
      drake.emit('drop', item, parent, _source, _currentSibling);
    }
    cleanup();
  }

  function cleanup () {
    var item = _copy || _item;
    ungrab();
    removeMirrorImage();
    if (item) {
      classes.rm(item, 'gu-transit');
    }
    if (_renderTimer) {
      clearTimeout(_renderTimer);
    }
    drake.dragging = false;
    if (_lastDropTarget) {
      drake.emit('out', item, _lastDropTarget, _source);
    }
    drake.emit('dragend', item);
    _source = _item = _copy = _initialSibling = _currentSibling = _renderTimer = _lastDropTarget = null;
  }

  function isInitialPlacement (target, s) {
    var sibling;
    if (s !== void 0) {
      sibling = s;
    } else if (_mirror) {
      sibling = _currentSibling;
    } else {
      sibling = nextEl(_copy || _item);
    }
    return target === _source && sibling === _initialSibling;
  }

  function findDropTarget (elementBehindCursor, clientX, clientY) {
    var target = elementBehindCursor;
    while (target && !accepted()) {
      target = getParent(target);
    }
    return target;

    function accepted () {
      var droppable = isContainer(target);
      if (droppable === false) {
        return false;
      }

      var immediate = getImmediateChild(target, elementBehindCursor);
      var reference = getReference(target, immediate, clientX, clientY);
      var initial = isInitialPlacement(target, reference);
      if (initial) {
        return true; // should always be able to drop it right back where it was
      }
      return o.accepts(_item, target, _source, reference);
    }
  }

  function drag (e) {
    if (!_mirror) {
      return;
    }
    e.preventDefault();

    var clientX = getCoord('clientX', e);
    var clientY = getCoord('clientY', e);
    var x = clientX - _offsetX;
    var y = clientY - _offsetY;

    _mirror.style.left = x + 'px';
    _mirror.style.top = y + 'px';

    var item = _copy || _item;
    var elementBehindCursor = getElementBehindPoint(_mirror, clientX, clientY);
    var dropTarget = findDropTarget(elementBehindCursor, clientX, clientY);
    var changed = dropTarget !== null && dropTarget !== _lastDropTarget;
    if (changed || dropTarget === null) {
      out();
      _lastDropTarget = dropTarget;
      over();
    }
    var parent = getParent(item);
    if (dropTarget === _source && _copy && !o.copySortSource) {
      if (parent) {
        parent.removeChild(item);
      }
      return;
    }
    var reference;
    var immediate = getImmediateChild(dropTarget, elementBehindCursor);
    if (immediate !== null) {
      reference = getReference(dropTarget, immediate, clientX, clientY);
    } else if (o.revertOnSpill === true && !_copy) {
      reference = _initialSibling;
      dropTarget = _source;
    } else {
      if (_copy && parent) {
        parent.removeChild(item);
      }
      return;
    }
    if (
      (reference === null && changed) ||
      reference !== item &&
      reference !== nextEl(item)
    ) {
      _currentSibling = reference;
      dropTarget.insertBefore(item, reference);
      drake.emit('shadow', item, dropTarget, _source);
    }
    function moved (type) { drake.emit(type, item, _lastDropTarget, _source); }
    function over () { if (changed) { moved('over'); } }
    function out () { if (_lastDropTarget) { moved('out'); } }
  }

  function spillOver (el) {
    classes.rm(el, 'gu-hide');
  }

  function spillOut (el) {
    if (drake.dragging) { classes.add(el, 'gu-hide'); }
  }

  function renderMirrorImage () {
    if (_mirror) {
      return;
    }
    var rect = _item.getBoundingClientRect();
    _mirror = _item.cloneNode(true);
    _mirror.style.width = getRectWidth(rect) + 'px';
    _mirror.style.height = getRectHeight(rect) + 'px';
    classes.rm(_mirror, 'gu-transit');
    classes.add(_mirror, 'gu-mirror');
    o.mirrorContainer.appendChild(_mirror);
    touchy(documentElement, 'add', 'mousemove', drag);
    classes.add(o.mirrorContainer, 'gu-unselectable');
    drake.emit('cloned', _mirror, _item, 'mirror');
  }

  function removeMirrorImage () {
    if (_mirror) {
      classes.rm(o.mirrorContainer, 'gu-unselectable');
      touchy(documentElement, 'remove', 'mousemove', drag);
      getParent(_mirror).removeChild(_mirror);
      _mirror = null;
    }
  }

  function getImmediateChild (dropTarget, target) {
    var immediate = target;
    while (immediate !== dropTarget && getParent(immediate) !== dropTarget) {
      immediate = getParent(immediate);
    }
    if (immediate === documentElement) {
      return null;
    }
    return immediate;
  }

  function getReference (dropTarget, target, x, y) {
    var horizontal = o.direction === 'horizontal';
    var reference = target !== dropTarget ? inside() : outside();
    return reference;

    function outside () { // slower, but able to figure out any position
      var len = dropTarget.children.length;
      var i;
      var el;
      var rect;
      for (i = 0; i < len; i++) {
        el = dropTarget.children[i];
        rect = el.getBoundingClientRect();
        if (horizontal && (rect.left + rect.width / 2) > x) { return el; }
        if (!horizontal && (rect.top + rect.height / 2) > y) { return el; }
      }
      return null;
    }

    function inside () { // faster, but only available if dropped inside a child element
      var rect = target.getBoundingClientRect();
      if (horizontal) {
        return resolve(x > rect.left + getRectWidth(rect) / 2);
      }
      return resolve(y > rect.top + getRectHeight(rect) / 2);
    }

    function resolve (after) {
      return after ? nextEl(target) : target;
    }
  }

  function isCopy (item, container) {
    return typeof o.copy === 'boolean' ? o.copy : o.copy(item, container);
  }
}


function touchy (el, op, type, fn) {
  var touch = {
    mouseup: 'touchend',
    mousedown: 'touchstart',
    mousemove: 'touchmove'
  };
  var pointers = {
    mouseup: 'pointerup',
    mousedown: 'pointerdown',
    mousemove: 'pointermove'
  };
  var microsoft = {
    mouseup: 'MSPointerUp',
    mousedown: 'MSPointerDown',
    mousemove: 'MSPointerMove'
  };
  if (global.navigator.pointerEnabled) {
    crossvent[op](el, pointers[type], fn);
  } else if (global.navigator.msPointerEnabled) {
    crossvent[op](el, microsoft[type], fn);
  } else {
    crossvent[op](el, touch[type], fn);
    crossvent[op](el, type, fn);
  }
}

function whichMouseButton (e) {
  if (e.touches !== void 0) { return e.touches.length; }
  if (e.which !== void 0 && e.which !== 0) { return e.which; } // see https://github.com/bevacqua/dragula/issues/261
  if (e.buttons !== void 0) { return e.buttons; }
  var button = e.button;
  if (button !== void 0) { // see https://github.com/jquery/jquery/blob/99e8ff1baa7ae341e94bb89c3e84570c7c3ad9ea/src/event.js#L573-L575
    return button & 1 ? 1 : button & 2 ? 3 : (button & 4 ? 2 : 0);
  }
}

function getOffset (el) {
  var rect = el.getBoundingClientRect();
  return {
    left: rect.left + getScroll('scrollLeft', 'pageXOffset'),
    top: rect.top + getScroll('scrollTop', 'pageYOffset')
  };
}

function getScroll (scrollProp, offsetProp) {
  if (typeof global[offsetProp] !== 'undefined') {
    return global[offsetProp];
  }
  if (documentElement.clientHeight) {
    return documentElement[scrollProp];
  }
  return doc.body[scrollProp];
}

function getElementBehindPoint (point, x, y) {
  var p = point || {};
  var state = p.className;
  var el;
  p.className += ' gu-hide';
  el = doc.elementFromPoint(x, y);
  p.className = state;
  return el;
}

function never () { return false; }
function always () { return true; }
function getRectWidth (rect) { return rect.width || (rect.right - rect.left); }
function getRectHeight (rect) { return rect.height || (rect.bottom - rect.top); }
function getParent (el) { return el.parentNode === doc ? null : el.parentNode; }
function isInput (el) { return el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.tagName === 'SELECT' || isEditable(el); }
function isEditable (el) {
  if (!el) { return false; } // no parents were editable
  if (el.contentEditable === 'false') { return false; } // stop the lookup
  if (el.contentEditable === 'true') { return true; } // found a contentEditable element in the chain
  return isEditable(getParent(el)); // contentEditable is set to 'inherit'
}

function nextEl (el) {
  return el.nextElementSibling || manually();
  function manually () {
    var sibling = el;
    do {
      sibling = sibling.nextSibling;
    } while (sibling && sibling.nodeType !== 1);
    return sibling;
  }
}

function getEventHost (e) {
  // on touchend event, we have to use `e.changedTouches`
  // see http://stackoverflow.com/questions/7192563/touchend-event-properties
  // see https://github.com/bevacqua/dragula/issues/34
  if (e.targetTouches && e.targetTouches.length) {
    return e.targetTouches[0];
  }
  if (e.changedTouches && e.changedTouches.length) {
    return e.changedTouches[0];
  }
  return e;
}

function getCoord (coord, e) {
  var host = getEventHost(e);
  var missMap = {
    pageX: 'clientX', // IE8
    pageY: 'clientY' // IE8
  };
  if (coord in missMap && !(coord in host) && missMap[coord] in host) {
    coord = missMap[coord];
  }
  return host[coord];
}


module.exports = dragula;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"./classes":8,"contra/emitter":4,"crossvent":5}],10:[function(require,module,exports){
var si = typeof setImmediate === 'function', tick;
if (si) {
  tick = function (fn) { setImmediate(fn); };
} else {
  tick = function (fn) { setTimeout(fn, 0); };
}

module.exports = tick;
},{}]},{},[1]);


