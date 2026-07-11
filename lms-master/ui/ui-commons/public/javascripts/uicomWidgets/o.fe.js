//document.getElementsByClassName('kix-paragraphrenderer')[0]


//sources of symbols
//http://www.alanwood.net/demos/symbol.html
//http://symbolcodes.tlt.psu.edu/bylanguage/mathchart.html
//http://www.mcs.sdsmt.edu/ecorwin/math_chars.html
//http://www.alanwood.net/unicode/mathematical_operators.html
//http://en.wikibooks.org/wiki/Unicode/List_of_useful_symbols



var vFE = new (function($) {
    var feWrapper, feFamilies;
    this.init = function(params) {
        feWrapper = $("#FEWrapper");
        feWrapper.find(".feBlock").html("<div contenteditable='true' \n\
         class='fePHTrans fePHTransInitial'></div>");
        feFamilies = feToolBars.feFamilies;
        feToolBars.init();
        feEqnMaker.init();
    };




    var makeHTMLTag = function(tag, attrs) {
        var el = document.createElement(tag);
        if (attrs) {
            for (var k in attrs)
                el.setAttribute(k, attrs[k]);
        }
        return $(el);
    };
    (function($) {
        $.fn.disableSelection = function() {
            return this
                    .css('user-select', 'none')
        };
    })(jQuery);
    var cancelEvent = function(e) {
        e = e || window.event;
        if (e.preventDefault && e.stopPropagation)
        {
            e.preventDefault();
            e.stopPropagation()
        }
        return false;
    };
    var getRelativeLeftPos = function(left) {
        return left - feWrapper.offset().left;
    };
    var getRelativeTopPos = function(top) {
        return top - feWrapper.offset().top;
    };
    var feEqnMaker = new (function($) {
        var feBlock, feTestDivForStringWidth;
        var feVerticalCursor, feHorizontalCursor, feTestDivForExtenderHeight;
        var feSelectionMarker, feComposerArea;
        var upArrow = "UP_ARROW", downArrow = "DOWN_ARROW", prevArrow = "LEFT_ARROW", nextArrow = "RIGHT_ARROW";
        var activeElForSelection, startPosXForSelection,
                selectionParentEl, elsSelected, targetPosForSelectionMarker,
                endElForSelection, isRemoveSelectionDone = true,
                isRightDirection = true, endPosXForSelection, feFinalSelection = {};
        var phClasses = ".feSmallPH,.fePH,.fePHTrans";
        var isctrlPressed = false, isshiftPressed = false, mousePosCounterForShift = 0;
        this.init = function() {
            feBlock = $("#feBlock");
            feTestDivForStringWidth = $("#feTestDivForStringWidth");
            feVerticalCursor = $("#feVerticalCursor");
            feHorizontalCursor = $("#feHorizontalCursor");
            feSelectionMarker = $("#feSelectionMarker");
            feComposerArea = $("#FEComposerArea");
            feTestDivForExtenderHeight = $("#feTestDivForExtenderHeight");
            var phTransInitial = feBlock.children(".fePHTransInitial");
            phTransInitial.focus();
            elsSelected = makeHTMLTag("div");
//            feComposerArea.disableSelection();
            //inserting equations 

            feBlock
                    .on('keydown', phClasses, fePHDivKeydown)
                    .on('keyup', phClasses, fePHDivKeyup)
                    .on('focus', phClasses, fePHDivFocus)
                    .on('blur', phClasses, fePHDivBlur)
                    .on("mousedown", phClasses, fePHDivMouseDown)
                    .on("click", phClasses, fePHDivClick)
                    .on("click", ".feEqnParent", feEqnParentClick)
                    .on("mousedown", ".feEqnParent", feEqnParentMouseDown)


            feWrapper.on("click", ".feClose",closeFEClick)
                    .on("click", ".feSubmitEqn", submitFEEqnClick)
                    .on("mousedown", "#feSelectionMarker", function() {
                        $(this).hide();
                    }).on("click", "#FEComposerAreaHolder", function(e) {
                        feBlock.children(".fePHTrans").last().focus();
                    });





            //to clear the selection when a click is made anywhere
            $(document).on("click", document, function(e) {
                var elClicked = $(e.target);
                if (!(elClicked.closest("#feBlock").length > 0
                        || elClicked.closest("#FEToolBar").length > 0)) {
                    removeSelection();
                }
            });
            $(document).on("click", ".copyEls", function(e) {
                e.stopPropagation();
                prepareSelectedEls(false);
                var obj = feFinalSelection;
                if (obj.type === "TEXT") {
                    $("#fePlayground").html(obj.text);
                } else {
                    $("#fePlayground").html(obj.elsSelected);
                }
            });
            $(document).on("click", ".cutEls", function(e) {
                e.stopPropagation();
                prepareSelectedEls(true);
                var obj = feFinalSelection;
                if (obj.type === "TEXT") {
                    $("#fePlayground").html(obj.text);
                } else {
                    $("#fePlayground").html(obj.elsSelected);
                }
                feSelectionMarker.hide();
            });
            $(document).on("click", ".getPlayEqn", function(e) {
                e.stopPropagation();
                var eqn = makeLatexEqn();
                $("#fePlayground").html(eqn);
                MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
            });

        };
        var fePHDivFocus = function() {
            feBlock.find(".feEqnParentBSDel").removeClass("feEqnParentBSDel");
            setCursorPosition();
        };
        var prepareSelectionEvents = function() {
            feWrapper.off("mousemove.feSelection")
                    .on("mousemove.feSelection", "#FEComposerAreaHolder", mouseMoveForSelection);
            $(document).unbind("mouseup.feSelection").bind("mouseup.feSelection", function(e) {
                feWrapper.off("mousemove.feSelection");
            });
            if (activeElForSelection.hasClass("fePHTrans")) {
                setSelectionParentEl();
            }
        };
        var prepareSelectedEls = function(removeSelectedEls, removeType) {
            if (isRemoveSelectionDone) {
                return null;
            }

            var feEqnParentForTheSelection;
            if (isElAnFETextNode(activeElForSelection)
                    && activeElForSelection.is(endElForSelection)) {
                var textSelectionObj = getTextPortionsInTheSelection(activeElForSelection);
                feFinalSelection.type = "TEXT";
                feFinalSelection.elsSelected = null;
                feFinalSelection.text = textSelectionObj.textSelected;
                feFinalSelection.removeType = removeType;

                if (removeSelectedEls) {
                    var lText = textSelectionObj.textUnSelectedLeft;
                    var newText = lText + textSelectionObj.textUnSelectedRight;
                    activeElForSelection.text(newText);
                    fePHAssignMaturity(activeElForSelection);
                    setFocusAndCursorPos(activeElForSelection.get(0), null, lText.length);
                    //****** focus is not taken care of after new el is created
                }
            } else {
                //this case only for fePHTrans because feph and fesmallph cannot be split selected

                var finalLeftText, finalLeftSelectedText, finalRightText,
                        finalRightSelectedText;
                var tsObj1 = getTextPortionsInTheSelection(activeElForSelection);
                var tsObj2 = getTextPortionsInTheSelection(endElForSelection);
                if (isRightDirection) {
                    finalLeftText = tsObj1.textUnSelectedLeft;
                    finalLeftSelectedText = tsObj1.textSelected;
                    finalRightText = tsObj2.textUnSelectedRight;
                    finalRightSelectedText = tsObj2.textSelected;
                } else {
                    finalRightText = tsObj1.textUnSelectedRight;
                    finalRightSelectedText = tsObj1.textSelected;
                    finalLeftText = tsObj2.textUnSelectedLeft;
                    finalLeftSelectedText = tsObj2.textSelected;
                }
                elsSelected.prepend(getfePHTransEl().text(finalLeftSelectedText));
                elsSelected.append(getfePHTransEl().text(finalRightSelectedText));
                if (removeSelectedEls) {
                    var newText = finalLeftText + finalRightText;
                    var finalEl = mergeTwoSeparateTextNodes(activeElForSelection, endElForSelection, newText);
                    setFocusAndCursorPos(finalEl.get(0), null, finalLeftText.length);
                    feEqnParentForTheSelection = finalEl.closest(".feEqnParent");
                }
                feFinalSelection.type = "COMBO";
                feFinalSelection.text = null;
                feFinalSelection.removeType = removeType;
                feFinalSelection.elsSelected = elsSelected.clone(true);
            }
            if (removeSelectedEls) {
                feBlock.find(".feSelected").remove();
                if (feEqnParentForTheSelection) {
                    checkAndCallOnChangeCallback({changeType: "DELETE"}, feEqnParentForTheSelection);
                }
            }
        };
        var setSelectionParentEl = function() {
            selectionParentEl = activeElForSelection
                    .closest("#feBlock,.fePHDiv,.feSmallPHDiv");
        };
        var isElAnFETextNode = function(el) {
            return (el.hasClass("fePHTrans") || el.hasClass("fePH") || el.hasClass("feSmallPH"));
        };
        var removeSelection = function() {
            elsSelected.html("");
            feBlock.find(".feSelected").removeClass("feSelected");
            feSelectionMarker.hide();
            isRemoveSelectionDone = true;
        };
        var mergefePHEls = function(holder) {
            holder.find(phClasses).each(function() {
                var nextEl = $(this).next(phClasses);
                if (nextEl.length > 0) {
                    nextEl.text($(this).text() + nextEl.text());
                    $(this).remove();
                }
            });
        };
        var mouseMoveForSelection = function(e, currentX) {
            currentX = currentX || e.pageX;
            //****** if the end is beyond feBlock then adjust the end accordingly
            endPosXForSelection = currentX;
            isRemoveSelectionDone = false;
            elsSelected = makeHTMLTag("div");
            if (currentX > startPosXForSelection) {
                isRightDirection = true;
            } else {
                isRightDirection = false;
            }
            var selParentOffsetLeft = selectionParentEl.offset().left;
            var selParentWidth = selectionParentEl.width();
            if (activeElForSelection.hasClass("fePH")
                    || activeElForSelection.hasClass("feSmallPH")) {
                setSelectionParentEl();
                var feEqnParentForfePH = activeElForSelection.closest(".feEqnParent");
                var feEqnParentForfePHOffsetLeft = feEqnParentForfePH.offset().left;
                var feEqnParentForfePHWidth = feEqnParentForfePH.width();
                if (isRightDirection && (currentX > (activeElForSelection.offset().left +
                        activeElForSelection.width()))) {
                    activeElForSelection = feEqnParentForfePH.prev(".fePHTrans");
                    startPosXForSelection = feEqnParentForfePHOffsetLeft;
                    setFocusAndCursorPos(activeElForSelection.get(0), prevArrow);
                    setCursorPosition();
                    targetPosForSelectionMarker = feEqnParentForfePHOffsetLeft + feEqnParentForfePHWidth;
                    endElForSelection = feEqnParentForfePH;
                    setSelectionParentEl();
                } else if (!isRightDirection && (currentX < activeElForSelection.offset().left)) {
                    activeElForSelection = feEqnParentForfePH.next(".fePHTrans");
                    setFocusAndCursorPos(activeElForSelection.get(0));
                    setCursorPosition();
                    startPosXForSelection = feEqnParentForfePHOffsetLeft + feEqnParentForfePHWidth;
                    targetPosForSelectionMarker = feEqnParentForfePHOffsetLeft;
                    endElForSelection = feEqnParentForfePH;
                    setSelectionParentEl();
                } else {
                    addPortionOfPHToSelection(activeElForSelection, currentX);
                }

            } else {
                var elementsToCheck = activeElForSelection;
                if (isRightDirection) {
                    if (currentX > (selParentOffsetLeft + selParentWidth) && !selectionParentEl.hasClass("feBlockClass")) {
                        var newFeEqnParent = selectionParentEl.closest(".feEqnParent");
                        var newFeEqnParentOffsetLeft = newFeEqnParent.offset().left;
                        var newFeEqnParentWidth = newFeEqnParent.width();
                        activeElForSelection = newFeEqnParent.prev(".fePHTrans");
                        setFocusAndCursorPos(activeElForSelection.get(0), prevArrow);
                        setCursorPosition();
                        startPosXForSelection = newFeEqnParentOffsetLeft;
                        targetPosForSelectionMarker = newFeEqnParentOffsetLeft + newFeEqnParentWidth;
                        elsSelected.html(selectionParentEl.clone(true));
                        endElForSelection = newFeEqnParent;
                        setSelectionParentEl();
                    } else {
                        elementsToCheck = elementsToCheck.add(activeElForSelection.nextAll());
                        for (var k = 0; k < elementsToCheck.length; k++) {
                            var el = elementsToCheck.eq(k);
                            if (el.hasClass("feEqnParent") && currentX > (el.offset().left + (el.width() / 2))) {
                                selectThisEl(el);
                            } else if (el.hasClass("fePHTrans")) {
                                addPortionOfPHToSelection(el, currentX);
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    if (currentX < (selParentOffsetLeft + selParentWidth) &&
                            !selectionParentEl.hasClass("feBlockClass")) {
                        var newFeEqnParent = selectionParentEl.closest(".feEqnParent");
                        var newFeEqnParentOffsetLeft = newFeEqnParent.offset().left;
                        var newFeEqnParentWidth = newFeEqnParent.width();
                        activeElForSelection = newFeEqnParent.next(".fePHTrans");
                        setFocusAndCursorPos(activeElForSelection.get(0));
                        setCursorPosition();
                        startPosXForSelection = newFeEqnParentOffsetLeft + newFeEqnParentWidth;
                        targetPosForSelectionMarker = newFeEqnParentOffsetLeft;
                        elsSelected.html(selectionParentEl.clone(true));
                        endElForSelection = newFeEqnParent;
                        setSelectionParentEl();
                    } else {
                        elementsToCheck = activeElForSelection.prevAll().add(elementsToCheck);
                        for (var k = elementsToCheck.length - 1; k >= 0; k--) {
                            var el = elementsToCheck.eq(k);
                            if (el.hasClass("feEqnParent") && currentX < (el.offset().left + (el.width() / 2))) {
                                selectThisEl(el);
                            } else if (el.hasClass("fePHTrans")) {
                                addPortionOfPHToSelection(el, currentX);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }


            var selHeight = selectionParentEl.height();
            var leftForSelection = startPosXForSelection;
            if (!isRightDirection) {
                leftForSelection = targetPosForSelectionMarker;
            }
            feSelectionMarker.height(selHeight)
                    .css({left: getRelativeLeftPos(leftForSelection), top: getRelativeTopPos(selectionParentEl.offset().top)})
                    .width(Math.abs(startPosXForSelection - targetPosForSelectionMarker)).show();

        };
        var selectThisEl = function(el) {
            el.addClass("feSelected");
            endElForSelection = el;
            if (isRightDirection) {
                targetPosForSelectionMarker = el.offset().left + el.width();
                elsSelected.append(el.clone(true));
            } else {
                targetPosForSelectionMarker = el.offset().left;
                elsSelected.prepend(el.clone(true));
            }
        };
        var addPortionOfPHToSelection = function(phEl, currentX) {
            var phElOffsetLeft = phEl.offset().left;
            var phElWidth = phEl.width();
            var phElTotalLeft = phElOffsetLeft + phElWidth;
            endElForSelection = phEl;


            if (isRightDirection) {
                if (startPosXForSelection < phElOffsetLeft && currentX > phElTotalLeft) {
                    selectThisEl(phEl);
                } else if (startPosXForSelection > phElTotalLeft || currentX < phElOffsetLeft) {
                    return;
                } else {
                    var actualStart = phElOffsetLeft, actualEnd = phElTotalLeft;
                    if (startPosXForSelection > actualStart) {
                        actualStart = startPosXForSelection;
                    }
                    if (currentX < actualEnd) {
                        actualEnd = currentX;
                    }
                    targetPosForSelectionMarker = actualEnd;
                }
            } else {
                if (startPosXForSelection > phElTotalLeft && currentX < phElOffsetLeft) {
                    selectThisEl(phEl);
                } else if (startPosXForSelection < phElOffsetLeft || currentX > phElTotalLeft) {
                    return;
                } else {
                    var actualStart = phElTotalLeft, actualEnd = phElOffsetLeft;
                    if (startPosXForSelection < actualStart) {
                        actualStart = startPosXForSelection;
                    }
                    if (currentX > actualEnd) {
                        actualEnd = currentX;
                    }
                    targetPosForSelectionMarker = actualEnd;
                }
            }
        };
        var getTextPortionsInTheSelection = function(phEl) {
            var phElOffsetLeft = phEl.offset().left;
            var phElWidth = phEl.width();
            var phElTotalLeft = phElOffsetLeft + phElWidth;
            var phElText = phEl.text();
            var fontSizeStr = phEl.css("font-size");
            //irrespective of the selection direction variable names will 
            //be with respect to selection in right direction
            var textUnSelectedLeft = "", textSelected = "", textUnSelectedRight = "";
            //giving a -+3px leeway for selection
            //******** this should actually be a function of font-size
            if (isRightDirection) {
                if ((startPosXForSelection - 3) < phElOffsetLeft && (endPosXForSelection + 3) > phElTotalLeft) {
                    textSelected = phElText;
                } else if (startPosXForSelection > phElTotalLeft || endPosXForSelection < phElOffsetLeft) {
                    if (startPosXForSelection > phElTotalLeft) {
                        textUnSelectedLeft = phElText;
                    } else {
                        textUnSelectedRight = phElText;
                    }
                } else {
                    var textParsed = "";
                    for (var k = 0; k < phElText.length; k++) {
                        var thisChar = phElText[k];
                        textParsed += thisChar;
                        var strParsedWidth = textParsed.stringWidth(fontSizeStr);
                        var testPos = strParsedWidth + phElOffsetLeft;
                        if (testPos < startPosXForSelection) {
                            textUnSelectedLeft += thisChar;
                        } else if (testPos > (endPosXForSelection + 3)) {
                            textUnSelectedRight += thisChar;
                        } else {
                            textSelected += thisChar;
                        }
                    }
                }
            } else {
                if ((startPosXForSelection + 3) > phElTotalLeft && (endPosXForSelection - 3) < phElOffsetLeft) {
                    textSelected = phEl.text();
                } else if (startPosXForSelection < phElOffsetLeft || endPosXForSelection > phElTotalLeft) {
                    if (startPosXForSelection < phElOffsetLeft) {
                        textUnSelectedRight = phElText;
                    } else {
                        textUnSelectedLeft = phElText;
                    }
                } else {
                    var textParsed = "";
                    for (var k = 0; k < phElText.length; k++) {
                        var thisChar = phElText[k];
                        textParsed += thisChar;
                        var strParsedWidth = textParsed.stringWidth(fontSizeStr);
                        var testPos = strParsedWidth + phElOffsetLeft;
                        if (testPos > startPosXForSelection + 3) {
                            textUnSelectedRight += thisChar;
                        } else if (testPos < endPosXForSelection) {
                            textUnSelectedLeft += thisChar;
                        } else {
                            textSelected += thisChar;
                        }
                    }
                }
            }
            return {textUnSelectedLeft: textUnSelectedLeft, textSelected: textSelected, textUnSelectedRight: textUnSelectedRight};
        };
        var getFeEqnDirectChild = function(feEqnParent, className) {
            return feEqnParent.find("." + className)
                    .not(feEqnParent.find(".feEqnParent").find("." + className));
        };
        var getfePHTransEl = function() {
            return makeHTMLTag("div", {"class": "fePHTrans", contenteditable: true});
        };
        var getfePHEl = function() {
            return makeHTMLTag("div", {"class": "fePH", contenteditable: true});
        };
        var getfeSmallPHEl = function() {
            return makeHTMLTag("div", {"class": "feSmallPH", contenteditable: true});
        };
        var mergeTwoSeparateTextNodes = function(leftEl, rightEl, newText) {
            var finalParentEl = leftEl.parent();
            var newfePHEl = getfePHTransEl();
            if (leftEl.siblings(".feEqnParent").not(".feSelected").length === 0) {
                if (finalParentEl.hasClass("fePHDiv")) {
                    newfePHEl = getfePHEl();
                } else if (finalParentEl.hasClass("feSmallPHDiv")) {
                    newfePHEl = getfeSmallPHEl();
                }
            }
            newfePHEl.text(newText);
            newfePHEl.insertBefore(leftEl);
            leftEl.remove();
            rightEl.remove();
            fePHAssignMaturity(newfePHEl);
            mergefePHEls(finalParentEl);
            if (finalParentEl.hasClass("feBlockClass")
                    && finalParentEl.children(".feEqnParent").not(".feSelected").length === 0) {
                finalParentEl.children(".fePHTrans").eq(0).addClass("fePHTransInitial");
            }
            return newfePHEl;
        };
        var fePHDivBlur = function() {
            feBlock.find(".feEqnParentBSDel").removeClass("feEqnParentBSDel");
            hideCursorPosition();
        };
        var fePHDivKeyup = function(e) {
            fePHAssignMaturity($(this));
            setCursorPosition();
            var key = e.keyCode || e.which;
            switch (key) {
                case 16:
                    {
                        isshiftPressed = false;
                        mousePosCounterForShift = 0;
                        break;
                    }
                case 17:
                    {
                        isctrlPressed = false;
                        break;
                    }
            }
        };
        var fePHDivClick = function(e) {
            e.stopPropagation();
            feWrapper.off("mousemove.feSelection");
            $(this).focus();
        };
        var fePHDivMouseDown = function(e) {
            removeSelection();
            e.stopPropagation();
            targetPosForSelectionMarker = startPosXForSelection = e.pageX;

            endElForSelection = activeElForSelection = $(this);
            prepareSelectionEvents();
        };
        var feEqnParentMouseDown = function(e) {
            removeSelection();
            var phTrans = $(this).prev(".fePHTrans");
            setTimeout(function() {
                setFocusAndCursorPos(phTrans.get(0), prevArrow);
                setCursorPosition();
            }, 1);
            endElForSelection = activeElForSelection = phTrans;
            e.stopPropagation();
            targetPosForSelectionMarker = startPosXForSelection = $(this).offset().left;
            prepareSelectionEvents();
        };
        var feEqnParentClick = function(e) {
            feWrapper.off("mousemove.feSelection");
            e.stopPropagation();
        };


        var cutcopySelectionObj = {};
        var fePHDivKeydown = function(e) {
            var key = e.keyCode || e.which;
            var activefeEl = getActiveEl();
            var excludedKeys = [8, 46, 17, 67, 88, 86];
            if (excludedKeys.indexOf(key) === -1 && !isRemoveSelectionDone) {
                removeSelection();
            }
            hideCursorPosition();
            var feEqnParent = activefeEl.closest(".feEqnParent");
            var anchorOffset = window.getSelection().anchorOffset;
            switch (key) {
                case 37:
                    {
                        //left arrow
                        if (isshiftPressed) {
                            mousePosCounterForShift -= 15;
                            mouseMoveForSelection(null, mousePosCounterForShift);
                        } else {
                            if (anchorOffset === 0) {
                                feNextPrevChild(e, prevArrow);
                            }
                        }
                        break;
                    }
                case 38:
                    {
                        //up arrow
                        feNextPrevChild(e, upArrow);
                        break;
                    }
                case 39:
                    {
                        //right arrow
                        if (isshiftPressed) {
                            mousePosCounterForShift += 15;
                            mouseMoveForSelection(null, mousePosCounterForShift);
                        } else {
                            if (activefeEl && (activefeEl.text().length === anchorOffset)) {
                                feNextPrevChild(e, nextArrow);
                            }
                        }

                        break;
                    }
                case 40:
                    {
                        //down arrow
                        feNextPrevChild(e, downArrow);
                        break;
                    }
                case 13:
                    {
                        //enter
                        e.preventDefault();
                        break;
                    }
                case 8:
                    {
                        //backspace
                        if (!isRemoveSelectionDone) {
                            prepareSelectedEls(true, "DELETE");
                            removeSelection();
                            e.preventDefault();
                        } else {
                            if (anchorOffset === 0) {
                                fePHBSDel(key);
                            } else {
                                checkAndCallOnChangeCallback({changeType: "TYPING"}, feEqnParent);
                            }
                        }
                        break;
                    }
                case 46:
                    {
                        //del key
                        if (!isRemoveSelectionDone) {
                            prepareSelectedEls(true, "DELETE");
                            removeSelection();
                            e.preventDefault();
                        } else {
                            if (activefeEl && (activefeEl.text().length === anchorOffset)) {
                                fePHBSDel(key);
                            } else {
                                checkAndCallOnChangeCallback({changeType: "TYPING"}, feEqnParent);
                            }
                        }
                        break;
                    }
                case 16:
                    {
                        //shift
                        isshiftPressed = true;
                        removeSelection();
                        targetPosForSelectionMarker = startPosXForSelection = parseFloat(feVerticalCursor.css("left"));
                        mousePosCounterForShift = startPosXForSelection;
                        endElForSelection = activeElForSelection = activefeEl;
                        if (activeElForSelection.hasClass("fePHTrans")) {
                            setSelectionParentEl();
                        }
                        break;
                    }
                case 17:
                    {
                        //ctrl
                        isctrlPressed = true;
                        break;
                    }
                case 67:
                    {
                        //c
                        if (isctrlPressed) {
                            prepareSelectedEls(false, "COPY");
                            e.preventDefault();
                            $.extend(cutcopySelectionObj, feFinalSelection);
                        }
                        break;
                    }
                case 88:
                    {
                        //x
                        if (isctrlPressed) {
                            prepareSelectedEls(true, "CUT");
                            e.preventDefault();
                            $.extend(cutcopySelectionObj, feFinalSelection);
                            removeSelection();
                        }
                        break;
                    }
                case 86:
                    {
                        //v
                        if (isctrlPressed) {
                            e.preventDefault();
                            if (cutcopySelectionObj) {
                                if (cutcopySelectionObj.type === "TEXT") {
                                    insertTextAtTheFocusedPos(cutcopySelectionObj.text);
                                } else {
                                    putContentAtTheFocusedPos("PASTE", cutcopySelectionObj.elsSelected);
                                }
                            }
                        }
                        break;
                    }
                default:
                    {
                        checkAndCallOnChangeCallback({changeType: "TYPING"}, feEqnParent);
                    }
            }
        };
        var getActiveEl = function() {
            return $(window.getSelection().anchorNode).closest(".fePHTrans,.fePH,.feSmallPH");
        };
        //delete and backspace
        var fePHBSDel = function(keyCode) {
            var activeEl = getActiveEl(), elToRemove;
            if (activeEl.hasClass("fePHTrans")) {
                if (keyCode === 8) {
                    elToRemove = activeEl.prev(".feEqnParent");
                } else if (keyCode === 46) {
                    elToRemove = activeEl.next(".feEqnParent");
                }
            } else {
                if (keyCode === 8) {
                    elToRemove = activeEl.closest(".feEqnParent");
                } else if (keyCode === 46) {
                    elToRemove = activeEl.closest(".feEqnParent");
                }
            }
            if (activeEl && elToRemove.length > 0) {
                if (elToRemove.hasClass("feEqnParentBSDel")) {
                    elToRemove.remove();
                    var leftEl, rightEl;
                    if (keyCode === 8) {
                        rightEl = activeEl;
                        leftEl = rightEl.prev(".fePHTrans");
                    } else {
                        leftEl = activeEl;
                        rightEl = leftEl.next(".fePHTrans");
                    }

                    var finalParentEl = leftEl.parent();
                    var newText = leftEl.text() + rightEl.text();
                    var finalCursorPos = leftEl.text().length;
                    var finalEl = mergeTwoSeparateTextNodes(leftEl, rightEl, newText);
                    setFocusAndCursorPos(finalEl.get(0), null, finalCursorPos);
                    checkAndCallOnChangeCallback({changeType: "DELETE"},
                    finalParentEl.closest(".feEqnParent"));
                }
                else {
                    if (keyCode === 8) {
                        feChangeActiveEl(elToRemove.next(".fePHTrans"));
                    } else {
                        feChangeActiveEl(elToRemove.prev(".fePHTrans"));
                    }
                    elToRemove.addClass("feEqnParentBSDel");
                }
            }
        };
        //utilities for keyup,keydown functions
        var fePHAssignMaturity = function(el) {
            if (el.text().length > 0) {
                el.addClass("fePHMatured");
                el.parent().addClass("fePHDivMatured");
            }
            else {
                el.removeClass("fePHMatured");
                el.parent().removeClass("fePHDivMatured");
            }
        };
        var feChangeActiveEl = function(newActiveEl) {
            feBlock.find(".feEqnParentBSDel").removeClass("feEqnParentBSDel");
            if (newActiveEl) {
                newActiveEl.focus();
            }
        };
        var feNextPrevChild = function(e, arrowType) {
            e.preventDefault();
            var activeEl = getActiveEl();
            var feEqnParent = activeEl.closest(".feEqnParent");
            if (!activeEl) {
                return;
            }
            var targetEl = getTargetEl(activeEl, arrowType, feEqnParent);
            if (targetEl && targetEl.length > 0) {
                if (targetEl.hasClass("fePHTrans")) {
                    setFocusAndCursorPos(targetEl.get(0), arrowType);
                    return;
                } else if (targetEl.hasClass("fePHDiv")
                        || targetEl.hasClass("feSmallPHDiv")) {
                    //checking if the final target el has ph classes
                    var phTrans = targetEl.children(".fePHTrans");
                    var fePHDivs = targetEl.children(".fePH,.feSmallPH").first();
                    if (phTrans.length > 0) {
                        var finalPHTrans;
                        if (arrowType === prevArrow) {
                            finalPHTrans = phTrans.last();
                        } else {
                            finalPHTrans = phTrans.first();
                        }
                        setFocusAndCursorPos(finalPHTrans.get(0), arrowType);
                        return;
                    } else if (fePHDivs.length > 0) {
                        setFocusAndCursorPos(fePHDivs.get(0), arrowType);
                        return;
                    }
                }
            }
        };
        var getTargetEl = function(activeEl, arrowType, feEqnParent) {
            var targetEl = [], parentOfActiveEl;
            if (activeEl.hasClass("fePHTrans")) {
                var nextFeEqnParent = activeEl.next(".feEqnParent");
                var prevFeEqnParent = activeEl.prev(".feEqnParent");
                if (arrowType === nextArrow && nextFeEqnParent.length > 0) {
                    targetEl = getTargetElUtil(nextFeEqnParent, null, nextFeEqnParent.data().nextPrevRule,
                            "PLUS", arrowType, -1);
                } else if (arrowType === prevArrow && prevFeEqnParent.length > 0) {
                    var ruleSet = prevFeEqnParent.data().nextPrevRule;
                    targetEl = getTargetElUtil(prevFeEqnParent, null, ruleSet,
                            "MINUS", arrowType, ruleSet.length);
                }
                if (targetEl.length > 0) {
                    return targetEl;
                } else {
                    //this happens in the case when nextfeEqnParent or prevFeEqnParent are not there or 
                    //when a uparrow/downarrow is pressed
                    parentOfActiveEl = activeEl.closest(".feSmallPHDiv,.fePHDiv");
                    feEqnParent = parentOfActiveEl.closest(".feEqnParent");
                }
            } else {
                parentOfActiveEl = activeEl.parent();
            }
            if (parentOfActiveEl.length === 0) {
                return;
            }

            targetEl = getTargetElForThisParentEl(parentOfActiveEl, feEqnParent, arrowType);
            return targetEl;
        };
        var getTargetElForThisParentEl = function(parentOfActiveEl, feEqnParent, arrowType) {
            var targetEl = [];
            var ruleSetData = feEqnParent.data();
            if (arrowType === nextArrow) {
                targetEl = getTargetElUtil(feEqnParent, parentOfActiveEl,
                        ruleSetData.nextPrevRule, "PLUS", arrowType);
            } else if (arrowType === prevArrow) {
                targetEl = getTargetElUtil(feEqnParent, parentOfActiveEl,
                        ruleSetData.nextPrevRule, "MINUS", arrowType);
            } else if (arrowType === upArrow) {
                targetEl = getTargetElUtil(feEqnParent, parentOfActiveEl,
                        ruleSetData.upDownRule, "MINUS", arrowType);
            } else if (arrowType === downArrow) {
                targetEl = getTargetElUtil(feEqnParent, parentOfActiveEl,
                        ruleSetData.upDownRule, "PLUS", arrowType);
            }
            return targetEl;
        };
        var getTargetElUtil = function(feEqnParent, parentOfActiveEl, ruleSet,
                addType, arrowType, elsIndex) {
            if (!elsIndex) {
                var currentPtrClass = parentOfActiveEl.data("navigationPtrClass");
                elsIndex = ruleSet.indexOf(currentPtrClass);
                if (elsIndex === -1) {
                    return null;
                }
            }
            var targetEl = [];
            if (addType === "PLUS") {
                while (elsIndex < ruleSet.length && targetEl.length === 0) {
                    elsIndex++;
                    var classToFind = ruleSet[elsIndex];
                    targetEl = getFeEqnDirectChild(feEqnParent, classToFind);
                    if (targetEl.css("visibility") === "hidden") {
                        targetEl = [];
                    }
                }
            } else {
                while (elsIndex > 0 && targetEl.length === 0) {
                    elsIndex--;
                    var classToFind = ruleSet[elsIndex];
                    targetEl = getFeEqnDirectChild(feEqnParent, classToFind);
                    if (targetEl.css("visibility") === "hidden") {
                        targetEl = [];
                    }
                }
            }
            //for left and right arrow                
            if (targetEl.length === 0) {
                if (arrowType === nextArrow) {
                    targetEl = getFeEqnNextEl(feEqnParent);
                } else if (arrowType === prevArrow) {
                    targetEl = getFeEqnPrevEl(feEqnParent);
                } else if (arrowType === upArrow || arrowType === downArrow) {
                    var upperLevelParentOfActiveEl = feEqnParent.parents(".fePHDiv,.feSmallPHDiv").eq(0);
                    if (upperLevelParentOfActiveEl.length === 0) {
                        return null;
                    }
                    targetEl = getTargetElForThisParentEl(upperLevelParentOfActiveEl,
                            upperLevelParentOfActiveEl.closest(".feEqnParent"), arrowType);
                }
            }
            return targetEl;
        };
        var getFeEqnNextEl = function(feEqnParent) {
            return feEqnParent.next(".fePHTrans");
        };
        var getFeEqnPrevEl = function(feEqnParent) {
            return feEqnParent.prev(".fePHTrans");
        };
        var setFocusAndCursorPos = function(node, arrowType, cursorPos) {
            //to account for lag
            //node==> native javascript element, do a .get(0) on jQuery object
            if (node.childNodes.length > 0) {
                try {
                    var childNode = node.childNodes[0];
                    if (!cursorPos) {
                        cursorPos = 0;
                        var childNodeText = childNode.textContent;
                        if (arrowType === prevArrow) {
                            cursorPos = childNodeText.length;
                        }
                    }
                    var range = document.createRange();
                    range.setStart(childNode, cursorPos);
                    range.setEnd(childNode, cursorPos);
                    var sel = window.getSelection();
                    sel.removeAllRanges();
                    sel.addRange(range);
                } catch (err) {
                    //to handle index out of bounds exception
                }
            }
            $(node).focus();
        };


        var makeLatexEqn = function() {
            return "\\[" + getEqnOfPHpa(feBlock) + "\\]";
        };
        var getEqnOfPHpa = function(PHpa) {
            var latexStr = "";
            PHpa.children(".feEqnParent,.fePHTrans").each(function() {
                var $this = $(this);
                if ($this.hasClass("feEqnParent")) {
                    var feLatex = "";
                    var latexEqnModel = $this.data("latexEqnModel");
                    var index = latexEqnModel.indexOf("%");
                    while (index > -1) {
                        var nextIndex = latexEqnModel.indexOf("%", index + 1);
                        if (nextIndex > -1) {
                            var eqnTurn = latexEqnModel.substring(index + 1, nextIndex);
                            var eqnTurnElement = $this.find(".eqnTurn" + eqnTurn)
                                    .not($this.find(".feEqnParent").find(".eqnTurn" + eqnTurn));
                            latexEqnModel = latexEqnModel.replace("%" + eqnTurn + "%", getEqnOfPHpa(eqnTurnElement).trim());
                        }
                        index = latexEqnModel.indexOf("%", nextIndex + 1);
                    }
                    latexStr += latexEqnModel;
                }
                else {
                    var t = $this.text().trim();
                    t = t.replace(' ', '\\,');
                    latexStr += t;
                }
            });
            if (PHpa.children(".feEqnParent,.fePHTrans").length === 0) {
                var t = PHpa.text().trim();
                t = t.replace(' ', '\\,');
                latexStr = t;
            }
            return latexStr;
        };


        // for submit
        var submitFEEqnClick = function(e) {
            var latex = makeLatexEqn();            
            var temp = closeFEAndGetTemp($(this));
            temp.removeClass().addClass("RTELatex").attr("contenteditable", false)
            temp.html(makeHTMLTag("div", {
                "contenteditable": false,
                "class": "RTELatex"
            })).html(latex).data("latexEqn", latex);
//		var latexSpan = makeHTMLTag("span", {
//			class : "RTELatexSpan"
//		});
//		latexSpan.insertAfter(temp);
//		// $('.RTEArea').focus();
//                var r = latexSpan.get(0);
            var r = temp.get(0).nextSibling;
            if (!r) {
                r = document.createTextNode("");
                $(r).insertAfter(temp);
            }
            var range = document.createRange();
            range.setStart(r, 0);
            range.setEnd(r, 0);
            var sel = window.getSelection();
            sel.removeAllRanges();
            sel.addRange(range);
//                range.collapse(true);
//		$(r).remove();
            MathJax.Hub.Queue(["Typeset", MathJax.Hub, temp.get(0)]);
            if (temp.next("br").length > 0) {
                temp.next("br").remove();
            } else if (temp.next("br[type='_moz']").length > 0) {
                temp.next("br[type='_moz']").remove();
                temp.next("br").remove();
            }
        };
        var closeFEClick = function() {
            closeFEAndGetTemp($(this)).remove();
        };
        var closeFEAndGetTemp = function($this) {
            $this.closest("#FEWrapperHolder").addClass("nonner");
            $("#errorPopupBlackOut").css("display", "none");
            var rteArea = vRTE.rteAreaForFE;
            rteArea.focus();
            return rteArea.find(".RTETempDiv");
        };

        //all sorts of utilities for inserting,aligning etc equations
        var alignFeExtenders = function(feEqnParent) {
            var diff = parseFloat(feEqnParent.css("top"));
            var extDiv = feEqnParent.closest(".feExtenders");
            if (extDiv.length > 0) {
                extDiv.css("top", -diff);
                var newExtPa = extDiv.closest(".feEqnParent");
                if (newExtPa.length > 0)
                    alignFeExtPa(newExtPa);
            } else {

            }
        };
        var timerForBlinkCursors;
        var setCursorPosition = function() {
            //the timeout function is used to take care of the lag,
            //otherwise it takes the previously focused element than the new focused element 
//            setTimeout(function() {
//
//            }, 1);
            var fePHElement = getActiveEl();
            if (!fePHElement) {
                return;
            }
            var targetParent;
            if (fePHElement.hasClass("fePH") || fePHElement.hasClass("feSmallPH")) {
                //setting the parent to be fePHElement. sometimes fePHDiv and feSmallPHDiv width is
                //100% in case of text align center cases as in fractions;
                targetParent = fePHElement;
            } else {
                //for fePHTrans
                //==height and width of feEqnParent||feBlock
                targetParent = fePHElement.parent("#feBlock,.fePHDiv,.feSmallPHDiv");
            }
            var fePHElementOffset = fePHElement.offset();
            var fePHElementOffsetLeft = fePHElementOffset.left;
            var targetParentOffset = targetParent.offset();
            var vertHeight = targetParent.height();
            var horiWidth = targetParent.width();
            var horiLeft = targetParentOffset.left + parseFloat(targetParent.css("padding-left"));

            var origText = fePHElement.text();
            var cursorPos = window.getSelection().anchorOffset;
            var t = origText.substring(0, cursorPos);

            var vertLeft = fePHElementOffsetLeft +
                    t.stringWidth(fePHElement.css("font-size"));

            feVerticalCursor.css({left: getRelativeLeftPos(vertLeft),
                top: getRelativeTopPos(targetParentOffset.top)
            }).height(vertHeight).show().removeClass("hider");

            feHorizontalCursor.css({left: getRelativeLeftPos(horiLeft),
                top: getRelativeTopPos((targetParentOffset.top + vertHeight))
            }).width(horiWidth + 1).show().removeClass("hider");
            clearInterval(timerForBlinkCursors);
            timerForBlinkCursors = setInterval(blinkCursors, 1200);
        };
        var blinkCursors = function() {
            feVerticalCursor.toggleClass("hider");
            feHorizontalCursor.toggleClass("hider");
        };
        var hideCursorPosition = function() {
            feVerticalCursor.hide();
            feHorizontalCursor.hide();
            if (timerForBlinkCursors) {
                clearInterval(timerForBlinkCursors);
            }
        };
        String.prototype.stringWidth = function(fontSizeStr) {
            feTestDivForStringWidth.text(this);
            if (fontSizeStr) {
                feTestDivForStringWidth.css("font-size", fontSizeStr);
            }
            var w = feTestDivForStringWidth.width();
            return w;
        };



        var insertThisEqn = function(e, fetbSubItem) {
            cancelEvent(e);
            var $this = $(this);
            if (fetbSubItem) {
                $this = fetbSubItem;
            }
            putContentAtTheFocusedPos("INSERT", $this);
        };
        this.insertThisEqn = insertThisEqn;
        var insertTextAtTheFocusedPos = function(text) {
            text = text || '';
            if (!isRemoveSelectionDone) {
                prepareSelectedEls(true, "PASTE");
            }
            var activeElInfo = dissectActiveEl();
            if (!activeElInfo) {
                return;
            }
            var newText = activeElInfo.firstPHTransText + text + activeElInfo.secondPHTransText;
            var activeEl = activeElInfo.activeElement;
            activeEl.text(newText);
            var newCursorPos = activeElInfo.firstPHTransText.length + text.length;
            setFocusAndCursorPos(activeEl.get(0), null, newCursorPos);
            removeSelection();
        };
        var dissectActiveEl = function() {
            var selection = window.getSelection();
            var activeElement = $(selection.anchorNode).closest(".fePHTrans,.fePH,.feSmallPH");
            if (activeElement.length === 0) {
                feToolBars.hideFeFamilies();
                removeSelection();
                return null;
            }
            var origText = activeElement.text();
            var cursorPos = selection.anchorOffset;
            var firstPHTransText = origText.substring(0, cursorPos);
            var secondPHTransText = origText.substring(cursorPos);
            return {activeElement: activeElement, firstPHTransText: firstPHTransText,
                secondPHTransText: secondPHTransText};
        };
        var putContentAtTheFocusedPos = function(putType, contentHolder) {
            var hasSelection = false;
            if (!isRemoveSelectionDone) {
                //==>There is selection available
                hasSelection = true;
                prepareSelectedEls(true, putType);
            }
            var activeElInfo = dissectActiveEl();
            if (!activeElInfo) {
                return;
            }
            var activeElement = activeElInfo.activeElement;

            //forming PHTrans and inserting
            var newContents = makeHTMLTag("div");
            var firstPHTransText = activeElInfo.firstPHTransText;
            var secondPHTransText = activeElInfo.secondPHTransText;
            if (putType === "PASTE") {
                var phTranses = contentHolder.children(".fePHTrans");
                var firstPHTransInSelection = phTranses.first();
                firstPHTransText += firstPHTransInSelection.text();
                firstPHTransInSelection.remove();
                var secondPHTransInSelection = phTranses.last();
                secondPHTransText += secondPHTransInSelection.text();
                secondPHTransInSelection.remove();
            }

            var contentToPut = contentHolder.children().clone(true);
            newContents.html(getfePHTransEl().text(firstPHTransText));
            newContents.append(contentToPut);
            newContents.append(getfePHTransEl().text(secondPHTransText));
            newContents.find(".feSmallPH,.fePH").attr("contenteditable", true);
            //focusing on a ph
            var feEqnParentForCbFns;
            var newActiveEl = newContents.find(".fePH").first();

            if (newActiveEl.length === 0)
                newActiveEl = newContents.find(".feSmallPH").first();

            //removing the holder
            newContents.children().insertBefore(activeElement);

            activeElement.remove();

            if (newActiveEl.length > 0) {
                if (hasSelection && putType === "INSERT") {
                    if (feFinalSelection.type === "TEXT" && feFinalSelection.text.length > 0) {
                        newActiveEl.text(feFinalSelection.text);
                    } else if (feFinalSelection.type === "COMBO") {
                        var activePa = newActiveEl.parent();
                        activePa.html(feFinalSelection.elsSelected.children());
                        newActiveEl = activePa.children(".fePHTrans").first();
                        fePHAssignMaturity(newActiveEl);
                    }
                }
                feEqnParentForCbFns = newActiveEl.closest(".feEqnParent");
                newActiveEl.focus();
            }

            if (feEqnParentForCbFns) {
                //on insert function callback
                checkAndCallOnChangeCallback({changeType: putType},
                feEqnParentForCbFns);
            }
            //hide fefamilies
            feToolBars.hideFeFamilies();
            removeSelection();
            setCursorPosition();
        };
        var insertThisSymbol = function(e) {
            cancelEvent(e);
            if (!isRemoveSelectionDone) {
                //==>There is selection available
                prepareSelectedEls(true, "INSERT");
            }
            var selection = window.getSelection();
            var activeElement = $(selection.anchorNode).closest(".fePHTrans,.fePH,.feSmallPH");
            if (activeElement.length === 0) {
                feToolBars.hideFeFamilies();
                return;
            }
            //finding cursor pos
            var origText = activeElement.text();
            //origText=origText.replace("<br>","");
            var cursorPos = selection.anchorOffset;
            //forming PHTrans and inserting
            var newText = "";
            newText = origText.substring(0, cursorPos) + $(this).text() + origText.substring(cursorPos);
            activeElement.html(newText);
            cursorPos++;
            setFocusAndCursorPos(activeElement.get(0), null, cursorPos);
            setCursorPosition();
            feToolBars.hideFeFamilies();
            removeSelection();
        };
        this.insertThisSymbol = insertThisSymbol;
        var checkAndCallOnChangeCallback = function(params, feEqnParent) {
            if (!feEqnParent || feEqnParent.length === 0) {
                return;
            }
            var onChangeCallback = feEqnParent.data("onChangeCallback");
            if (onChangeCallback) {
                onChangeCallback(params, feEqnParent);
            }
            checkAndCallOnChangeCallback(params, feEqnParent.parents(".feEqnParent").eq(0));
        };
    })($);
    var feToolBars = new (function($) {
        var feToolBar;
        this.init = function() {
            feToolBar = $("#FEToolBar");
            feToolBar.off()
                    .on("click", ".FEFamilyHead", showFeFamily)
                    .on("mouseenter", ".FEFamilyHead", feFamilyHovered)
                    .on('mousedown', ".FETBSubItem,.FEFamilyHead,.FETBSubItemSymbol", cancelEvent)
                    .on('click', ".FETBSubItem", feEqnMaker.insertThisEqn)
                    .on('click', ".FETBSubItemSymbol", feEqnMaker.insertThisSymbol)
            prepareToolBars();
        };
        var prepareToolBars = function() {
            var feFamilyClonable = $("#FEFamilyClonable");
            $.each(feFamilies, function(familyName, familyFn) {
                var familyObj = new familyFn();
                var feFamilyHTML = feFamilyClonable.children().clone(true)
                        .addClass("FEFamily" + familyName);
                var subFamilyContent = feFamilyHTML.find(".FEFamilySubFamilyContent");
                if (familyObj.familyType === "SYMBOL") {
                    feFamilyHTML.addClass("FEFamilySymbols");
                    var symbols = familyObj.symbols;
                    for (var j = 0; j < symbols.length; j++) {
                        var symbol = symbols[j];
                        var symbolChar = symbol.characterEntity;
                        var feSubItem = makeHTMLTag("div", {"class": "FETBSubItemSymbol"})
                                .html(symbolChar);
                        subFamilyContent.append(feSubItem);
                        putFamilyIcon(feFamilyHTML, symbolChar, symbol.isFamilyIcon, 3);
                    }
                } else {
                    var variants = familyObj.variants;
                    if (!variants) {
                        return;
                    }
                    $.each(variants, function(subFamilyName, subFamilyFn) {
                        var variantObj = new subFamilyFn();
                        var variant = variantObj.getHTML;
                        var feSubItem = makeHTMLTag("div", {"class": "FETBSubItem"}).html(variant.clone(true));
                        feSubItem.find(".feEqnParent").data("latexEqnModel", variantObj.latexEqnModel);
                        subFamilyContent.append(feSubItem);

                        //for playground or testing
//                        if ((familyName === "FRAC_ROOT") || familyName === "EX") {
//                            feSubItem = makeHTMLTag("div", {"style": "display:inline-block"}).html(variant.clone(true));
//                            $("#fePlayground").append(feSubItem);
//                            $("#fePlayground").find(".fePHTrans,.feSmallPH,.fePH").attr("contenteditable", true);
//                        }

                        putFamilyIcon(feFamilyHTML, variant.clone(true), variantObj.isFamilyIcon, 2);
                    });
                }
                feToolBar.append(feFamilyHTML);
            });
        };
        var putFamilyIcon = function(feFamilyHTML, variantHTML, isFamilyIcon, familyIconLimit) {
            var currentFamilyIconsCount = feFamilyHTML.find(".FETBSubItemForFamilyHead").length;
            if (isFamilyIcon && currentFamilyIconsCount < familyIconLimit) {
                var feSubItemForFamilyHead = makeHTMLTag("div",
                        {"class": "FETBSubItemForFamilyHead"}).html(variantHTML);
                feFamilyHTML.find(".FEFamilyHead").append(feSubItemForFamilyHead);
            }
        };
        var feFamilyHeadClicked;
        var showFeFamily = function(e) {
            if (feFamilyHeadClicked) {
                feFamilyHeadClicked.removeClass("FEFamilyClicked");
            }
            var feFamily = $(this).closest(".FEFamily");
            feFamilyHeadClicked = feFamily;
            feFamily.addClass("FEFamilyClicked");
            $(document).bind("click.familyHead", function(e) {
                var elClicked = $(e.target);
                if (elClicked.closest(".FEFamily").length === 0) {
                    hideFeFamilies();
                }
            });
        };
        var feFamilyHovered = function() {
            if (feFamilyHeadClicked) {
                feFamilyHeadClicked.removeClass("FEFamilyClicked");
                var feFamily = $(this).closest(".FEFamily");
                feFamilyHeadClicked = feFamily;
                feFamily.addClass("FEFamilyClicked");
            }
        };
        var hideFeFamilies = function() {
            if (feFamilyHeadClicked) {
                feFamilyHeadClicked.removeClass("FEFamilyClicked");
            }
            $(document).unbind("click.familyHead");
            feFamilyHeadClicked = null;
        };
        this.hideFeFamilies = hideFeFamilies;
        var htmlUtilities = function() {
            var smallPHClasses = "feSmallPHDiv";
            var getPH = function(val, latexEqnTurn, extraClasses) {
                extraClasses = (!extraClasses) ? "" : extraClasses;
                val = (!val) ? "" : val;
                var el = makeHTMLTag('div');
                el.attr("class",
                        "fePHDiv feCore eqnTurn" + latexEqnTurn + " " + extraClasses)
                el.html("<div class='fePH'>" + val + "</div>");
                el.data("navigationPtrClass", "feCore");
                return el;
            };
            var getULSmallPH = function(val, latexEqnTurn, extraClasses) {
                extraClasses = (!extraClasses) ? "" : extraClasses;
                val = (!val) ? "" : val;
                var el = makeHTMLTag('div');
                el.attr(
                        "class",
                        "feUL " + smallPHClasses
                        + " eqnTurn" + latexEqnTurn + " " + extraClasses);
                el.html("<div class='feSmallPH'>" + val + "</div>");
                el.data("navigationPtrClass", "feUL");
                return el;
            };
            var getLLSmallPH = function(val, latexEqnTurn, extraClasses) {
                extraClasses = (!extraClasses) ? "" : extraClasses;
                val = (!val) ? "" : val;
                var el = makeHTMLTag('div');
                el.attr(
                        "class",
                        "feLL " + smallPHClasses
                        + " eqnTurn" + latexEqnTurn + " " + extraClasses);
                el.html("<div class='feSmallPH'>" + val + "</div>");
                el.data("navigationPtrClass", "feLL");
                return el;
            };
            var getIcon = function(iconVal, extraClasses) {
                extraClasses = (!extraClasses) ? "" : extraClasses;
                var el = makeHTMLTag("div");
                el.attr("class",
                        "feIcon " + extraClasses)
                        .html(iconVal);
                return el;
            };
            this.getPH = getPH;
            this.getULSmallPH = getULSmallPH;
            this.getLLSmallPH = getLLSmallPH;
            this.getIcon = getIcon;
            var getContentFromTable = function(table, formation) {
                formation = formation || [];
                var tableContent = makeHTMLTag("table"), k = 1;
                for (var r = 0; r < table.tableOrder.rows; r++) {
                    var trContent = makeHTMLTag('tr');
                    for (var c = 0; c < table.tableOrder.columns; c++) {
                        var td = makeHTMLTag('td');
                        var cellCount = $.inArray(k, formation);
                        if (table["cell" + k] && cellCount > -1) {
                            var cell = table["cell" + k];
                            td.append(cell.clone(true));
                        }
                        trContent.append(td);
                        k++;
                    }
                    tableContent.append(trContent).attr("cellPadding", 0).attr("cellSpacing", 0);
                }
                return tableContent;
            };
            var wrapInFeEqnParent = function(eqnContentDiv, familyName, subFamilyName, formation, extraNameSpaceForClassNames, params) {
                var subFamilyName = subFamilyName || "feSubFamily";
                params = params || {};
                formation = formation || [];
                var nameSpaces = [familyName, subFamilyName];
                if (extraNameSpaceForClassNames) {
                    $.merge(nameSpaces, extraNameSpaceForClassNames.split(","));
                }
                var eqnParentClasses = getClassesForFeEqnChildren("feEqnParent", nameSpaces);
                eqnContentDiv.addClass(eqnParentClasses).data("family", familyName)
                        .data("subFamily", subFamilyName).data("params", params);
                if (formation.length > 0) {
                    eqnContentDiv.addClass("feEqnFormation" + formation.join(""));
                }

                eqnContentDiv.find(".alignerBlock").addClass(getClassesForFeEqnChildren("alignerBlock", nameSpaces));
                eqnContentDiv.find(".feCore").addClass(getClassesForFeEqnChildren("feCore", nameSpaces));
                eqnContentDiv.find(".feUL").addClass(getClassesForFeEqnChildren("feUL", nameSpaces));
                eqnContentDiv.find(".feLL").addClass(getClassesForFeEqnChildren("feLL", nameSpaces));
                eqnContentDiv.find(".feIcon").addClass(getClassesForFeEqnChildren("feIcon", nameSpaces));
                return eqnContentDiv;
            };
            var getClassesForFeEqnChildren = function(mainClass, nameSpaces) {
                var classes = mainClass;
                for (var k = 0; k < nameSpaces.length; k++) {
                    classes += " " + mainClass + nameSpaces[k];
                }
                return classes;
            };


            this.getContentFromTable = getContentFromTable;
            this.wrapInFeEqnParent = wrapInFeEqnParent;
            var getTable33 = function() {
                return {
                    tableOrder: {
                        rows: 3,
                        columns: 3
                    },
                    cell1: getULSmallPH(null, 1),
                    cell2: getULSmallPH(null, 2),
                    cell3: getULSmallPH(null, 3),
                    cell4: getPH(null, 4),
                    cell5: getPH(null, 5),
                    cell6: getPH(null, 6),
                    cell7: getLLSmallPH(null, 7),
                    cell8: getLLSmallPH(null, 8),
                    cell9: getLLSmallPH(null, 9),
                };
            };
            this.getTable33 = getTable33;
            var getAlignerBlockBalancer = function() {
                return makeHTMLTag("div", {class: "alignerBlockBalancer"});
            };
            this.getAlignerBlockBalancer = getAlignerBlockBalancer;

            var setNavigationRules = function(feEqnParent) {
                var nextPrevRule = ["feUL", "feLL", "feCore"];
                var upDownRule = ["feUL", "feCore", "feLL"];
                feEqnParent.data("nextPrevRule", nextPrevRule).data("upDownRule", upDownRule);
            };
            this.commonNavigationRules = setNavigationRules;
            //commons for sigma,integral,product and U and ∩
            this.integralRelatedEqns = function(familyName) {
                var prepareHTML = function(iconValue, subFamilyName, extraNameSpaces,
                        hasTopLimit, hasBottomLimit) {
                    var eqnContentDiv = makeHTMLTag("div");
                    var alignerBlock = makeHTMLTag("div", {class: "alignerBlock"});
                    if (hasTopLimit) {
                        alignerBlock.append(getULSmallPH(null, 2));
                    }
                    alignerBlock.append(getIcon(iconValue));
                    if (hasBottomLimit) {
                        alignerBlock.append(getLLSmallPH(null, 8));
                    }
                    eqnContentDiv.append(alignerBlock);
                    eqnContentDiv.append(getPH(null, 5));
                    setNavigationRules(eqnContentDiv);
                    var wrappedEqn = wrapInFeEqnParent(eqnContentDiv, familyName, subFamilyName, null, extraNameSpaces);
                    return wrappedEqn;
                };
                this.prepareHTML = prepareHTML;
            };
            this.getCSSTransformAttrs = function(scale) {
                var scaleStr = "scale(" + scale + ")";
                return {transform: scaleStr, "-webkit-transform": scaleStr,
                    "-moz-transform": scaleStr, "-ms-transform": scaleStr,
                    "-o-transform": scaleStr};
            };
        };
        var feFamilies = {
            COMPARATORS: function() {
                var familyName = "COMPARATORS";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&le;", isFamilyIcon: true},
                    {characterEntity: "&ge;"},
                    {characterEntity: "&ll;"},
                    {characterEntity: "&gg;"},
                    {characterEntity: "&prec;"},
                    {characterEntity: "&succ;"},
                    {characterEntity: "&triangleleft;"},
                    {characterEntity: "&triangleright;"},
                    {characterEntity: "&sim;"},
                    {characterEntity: "&approx;", isFamilyIcon: true},
                    {characterEntity: "&simeq;"},
                    {characterEntity: "&cong;"},
                    {characterEntity: "&ne;", isFamilyIcon: true},
                    {characterEntity: "&equiv;"},
                    {characterEntity: "&triangleq;"},
                    {characterEntity: "&doteq;"},
                    {characterEntity: "&propto;"}
                ];
            },
            OPERATORS: function() {
                var familyName = "GREEK_SMALL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&pm;", isFamilyIcon: true},
                    {characterEntity: "&mp;"},
                    {characterEntity: "&times;"},
                    {characterEntity: "*"},
                    {characterEntity: "&div;"},
                    {characterEntity: "&oplus;"},
                    {characterEntity: "&otimes;", isFamilyIcon: true},
                    {characterEntity: "&odot;"},
                    {characterEntity: "&#8729;"},
                    {characterEntity: "&#149;", isFamilyIcon: true},
                    {characterEntity: "&#8728;"},
                    {characterEntity: "&#9001;"},
                    {characterEntity: "&#9002;"}
                ]
            },
            ARROW_SYMBOLS: function() {
                var familyName = "GREEK_SMALL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&leftrightarrow;"},
                    {characterEntity: "&rightarrow;", isFamilyIcon: true},
                    {characterEntity: "&leftarrow;"},
                    {characterEntity: "&updownarrow;"},
                    {characterEntity: "&uparrow;"},
                    {characterEntity: "&downarrow;", isFamilyIcon: true},
                    {characterEntity: "&Leftrightarrow;", isFamilyIcon: true},
                    {characterEntity: "&Rightarrow;"},
                    {characterEntity: "&Leftarrow;"},
                    {characterEntity: "&Updownarrow;"},
                    {characterEntity: "&Uparrow;"},
                    {characterEntity: "&Downarrow;"},
                    {characterEntity: "&nearrow;"},
                    {characterEntity: "&swarrow;"},
                    {characterEntity: "&searrow;"},
                    {characterEntity: "&nwarrow;"},
                    {characterEntity: "&rightleftarrows;"},
                    {characterEntity: "&rightleftharpoons;"},
                    {characterEntity: "&mapsto;"}
                ]
            },
            STATEMENT_SYMBOLS: function() {
                var familyName = "GREEK_SMALL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&therefore;", isFamilyIcon: true},
                    {characterEntity: "&because;"},
                    {characterEntity: "&#8715;"},
                    {characterEntity: "&#8707;", isFamilyIcon: true},
                    {characterEntity: "&forall;", isFamilyIcon: true},
                    {characterEntity: "&not;"},
                    {characterEntity: "&wedge;"},
                    {characterEntity: "&vee;"}
                ]
            },
            SET: function() {
                var familyName = "GREEK_SMALL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&in;"},
                    {characterEntity: "&notin;", isFamilyIcon: true},
                    {characterEntity: "&cup;"},
                    {characterEntity: "&cap;", isFamilyIcon: true},
                    {characterEntity: "&cup;"},
                    {characterEntity: "&cap;"},
                    {characterEntity: "&subset;", isFamilyIcon: true},
                    {characterEntity: "&supset;"},
                    {characterEntity: "&subseteq;"},
                    {characterEntity: "&supseteq;"},
                    {characterEntity: "&not;"},
                    {characterEntity: "&subset;"},
                    {characterEntity: "&emptyset;"}
                ];
            },
            MATH_SYMBOLS: function() {
                var familyName = "GREEK_SMALL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&part;", isFamilyIcon: true},
                    {characterEntity: "&wp;"},
                    {characterEntity: "&Im;"},
                    {characterEntity: "&Re;"},
                    {characterEntity: "&aleph;"},
                    {characterEntity: "&infin;", isFamilyIcon: true},
                    {characterEntity: "&hbar;"},
                    {characterEntity: "&lambda;"},
                    {characterEntity: "&ell;", isFamilyIcon: true},
                    {characterEntity: "&#9768;"},
                    {characterEntity: "&Delta;"},
                    {characterEntity: "&nabla;"},
                    {characterEntity: "&Omega;"},
                    {characterEntity: "&mho;"},
                    {characterEntity: "&#9674;"},
                    {characterEntity: "&sum;"},
                    {characterEntity: "&prod;"},
                    {characterEntity: "&coprod;"},
                    {characterEntity: "&#8747;"},
                    {characterEntity: "&#9675;"},
                    {characterEntity: "&angle;"},
                    {characterEntity: "&measuredangle;"},
                    {characterEntity: "&#8738;"},
                    {characterEntity: "&bot;"},
                    {characterEntity: "&parallel;"},
                    {characterEntity: "&#916;"},
                    {characterEntity: "&square;"},
                    {characterEntity: "&bigcirc;"}
                ];
            },
            GREEK_SMALL: function() {
                var familyName = "GREEK_SMALL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "&alpha;"},
                    {characterEntity: "&beta;"},
                    {characterEntity: "&chi;"},
                    {characterEntity: "&delta;"},
                    {characterEntity: "&varepsilon;"},
                    {characterEntity: "&phi;"},
                    {characterEntity: "&varphi;"},
                    {characterEntity: "&gamma;"},
                    {characterEntity: "&eta;"},
                    {characterEntity: "&iota;"},
                    {characterEntity: "&kappa;"},
                    {characterEntity: "&lambda;", isFamilyIcon: true},
                    {characterEntity: "&mu;"},
                    {characterEntity: "&nu;"},
                    {characterEntity: "o"},
                    {characterEntity: "&pi;"},
                    {characterEntity: "&varpi;"},
                    {characterEntity: "&theta;", isFamilyIcon: true},
                    {characterEntity: "&vartheta;"},
                    {characterEntity: "&rho;"},
                    {characterEntity: "&sigma;"},
                    {characterEntity: "&varsigma;"},
                    {characterEntity: "&tau;"},
                    {characterEntity: "&upsilon;"},
                    {characterEntity: "&omega;", isFamilyIcon: true},
                    {characterEntity: "&xi;"},
                    {characterEntity: "&psi;"},
                    {characterEntity: "&zeta;"},
                ]
            },
            GREEK_CAPITAL: function() {
                var familyName = "GREEK_CAPITAL";
                this.familyType = "SYMBOL";
                this.familyName = familyName;
                this.symbols = [
                    {characterEntity: "A"},
                    {characterEntity: "B"},
                    {characterEntity: "X"},
                    {characterEntity: "&Delta;"},
                    {characterEntity: "E"},
                    {characterEntity: "&Phi;"},
                    {characterEntity: "&Gamma;"},
                    {characterEntity: "H"},
                    {characterEntity: "I"},
                    {characterEntity: "K"},
                    {characterEntity: "&Lambda;", isFamilyIcon: true},
                    {characterEntity: "M"},
                    {characterEntity: "N"},
                    {characterEntity: "O"},
                    {characterEntity: "&Pi;"},
                    {characterEntity: "&Theta;", isFamilyIcon: true},
                    {characterEntity: "P"},
                    {characterEntity: "&Sigma;"},
                    {characterEntity: "T"},
                    {characterEntity: "&Upsilon;"},
                    {characterEntity: "&Omega;", isFamilyIcon: true},
                    {characterEntity: "&Xi;"},
                    {characterEntity: "&Psi;"},
                    {characterEntity: "Z"}
                ]
            },
            BRACES: function() {
                var familyObj = $.extend(this, new htmlUtilities());
                var familyName = "BRACES";
                this.familyName = familyName;
                var setNavigationRules = function(feEqnParent) {
                    var nextPrevRule = ["feCore"];
                    var upDownRule = nextPrevRule;
                    feEqnParent.data("nextPrevRule", nextPrevRule).data("upDownRule", upDownRule);
                };
                var prepareHTML = function(leftIconValue, rightIconValue, subFamilyName) {
                    var eqnContentDiv = makeHTMLTag("div");
                    if (leftIconValue) {
                        eqnContentDiv.append(familyObj.getIcon(leftIconValue));
                    }
                    eqnContentDiv.append(familyObj.getPH(null, 5));
                    if (rightIconValue) {
                        eqnContentDiv.append(familyObj.getIcon(rightIconValue));
                    }
                    eqnContentDiv.addClass("feExtendersPa").data("onChangeCallback", onChangeCallback);
                    setNavigationRules(eqnContentDiv);
                    var wrappedEqn = familyObj.wrapInFeEqnParent(eqnContentDiv, familyName, subFamilyName);
                    return wrappedEqn;
                };
                var variants = {
                    "SIMPLE": function() {
                        var name = "SIMPLE";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\left({%5%}\\right)";
                        this.getHTML = prepareHTML("&#40;", "&#41;", name);
                    },
                    "SQUARE": function() {
                        var name = "SQUARE";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\left[{%5%}\\right]";
                        this.getHTML = prepareHTML("&#91;", "&#93;", name);
                    },
                    "FLOWER": function() {
                        var name = "FLOWER";
                        this.name = name;
                        this.latexEqnModel = "\\left\\{{%5%}\\right\\}";
                        this.getHTML = prepareHTML("&#123;", "&#125;", name);
                    },
                    "ANGLE": function() {
                        var name = "ANGLE";
                        this.name = name;
                        this.latexEqnModel = "\\left\\langle{%5%}\\right\\rangle";
                        this.getHTML = prepareHTML("&#9001;", "&#9002;", name);
                    },
                    "LINE": function() {
                        var name = "LINE";
                        this.name = name;
                        this.latexEqnModel = "\\left|{%5%}\\right|";
                        this.getHTML = prepareHTML("&#124;", "&#124;", name);
                    },
                    "DOUBLE_LINE": function() {
                        var name = "DOUBLE_LINE";
                        this.name = name;
                        this.latexEqnModel = "\\left\\|{%5%}\\right\\|";
                        this.getHTML = prepareHTML("&#8214;", "&#8214;", name);
                    },
                    "SQUARE_DOWN": function() {
                        var name = "SQUARE_DOWN";
                        this.name = name;
                        this.latexEqnModel = "\\left\\lfloor{%5%}\\right\\rfloor";
                        this.getHTML = prepareHTML("&#9123;", "&#9126;", name);
                    },
                    "SQUARE_UP": function() {
                        var name = "SQUARE_DOWN";
                        this.name = name;
                        this.latexEqnModel = "\\left\\lceil{%5%}\\right\\rceil";
                        this.getHTML = prepareHTML("&#9121;", "&#9124;", name);
                    },
                    "SQUARE_SIMPLE": function() {
                        var name = "SQUARE_SIMPLE";
                        this.name = name;
                        this.latexEqnModel = "\\left[{%5%}\\right)";
                        this.getHTML = prepareHTML("&#91;", "&#41;", name);
                    },
                    "SIMPLE_SQUARE": function() {
                        var name = "SIMPLE_SQUARE";
                        this.name = name;
                        this.latexEqnModel = "\\left({%5%}\\right]";
                        this.getHTML = prepareHTML("&#40;", "&#93;", name);
                    },
                    "LINE_ANGLE": function() {
                        var name = "LINE_ANGLE";
                        this.name = name;
                        this.latexEqnModel = "\\left|{%5%}\\right\\rangle";
                        this.getHTML = prepareHTML("&#124;", "&#9002;", name);
                    },
                    "ANGLE_LINE": function() {
                        var name = "ANGLE_LINE";
                        this.name = name;
                        this.latexEqnModel = "\\left\\langle{%5%}\\right|";
                        this.getHTML = prepareHTML("&#9001;", "&#124;", name);
                    },
                    "SQUARE_SQUARE_LEFT_OPEN": function() {
                        var name = "SQUARE_SQUARE_LEFT_OPEN";
                        this.name = name;
                        this.latexEqnModel = "\\left[{%5%}\\right[";
                        this.getHTML = prepareHTML("&#91;", "&#91;", name);
                    },
                    "SQUARE_SQUARE_RIGHT_OPEN": function() {
                        var name = "SQUARE_SQUARE_RIGHT_OPEN";
                        this.name = name;
                        this.latexEqnModel = "\\left]{%5%}\\right]";
                        this.getHTML = prepareHTML("&#93;", "&#93;", name);
                    },
                    "SQUARE_SQUARE_INVERT": function() {
                        var name = "SQUARE_SQUARE_INVERT";
                        this.name = name;
                        this.latexEqnModel = "\\left]{%5%}\\right[";
                        this.getHTML = prepareHTML("&#93;", "&#91;", name);
                    },
                    "DOUBLE_SQUARE": function() {
                        var name = "DOUBLE_SQUARE";
                        this.name = name;
                        this.latexEqnModel = "\\left[\\kern-0.15em\\left[{%5%}\\right]\\kern-0.15em\\right]";
                        //****unicode not found,so using simple square
                        this.getHTML = prepareHTML("&#91;", "&#93;", name);
                    },
                    "SIMPLE_LEFT": function() {
                        var name = "SIMPLE_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left({%5%}";
                        this.getHTML = prepareHTML("&#40;", null, name);
                    },
                    "SIMPLE_RIGHT": function() {
                        var name = "SIMPLE_RIGHT";
                        this.name = name;
                        this.latexEqnModel = "{%5%}\\right)";
                        this.getHTML = prepareHTML(null, "&#41;", name);
                    },
                    "SQUARE_LEFT": function() {
                        var name = "SQUARE_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left[{%5%}";
                        this.getHTML = prepareHTML("&#91;", null, name);
                    },
                    "SQUARE_RIGHT": function() {
                        var name = "SQUARE_RIGHT";
                        this.name = name;
                        this.latexEqnModel = "{%5%}\\right]";
                        this.getHTML = prepareHTML(null, "&#93;", name);
                    },
                    "FLOWER_LEFT": function() {
                        var name = "FLOWER_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left\\{{%5%}";
                        this.getHTML = prepareHTML("&#123;", null, name);
                    },
                    "FLOWER_RIGHT": function() {
                        var name = "FLOWER_RIGHT";
                        this.name = name;
                        this.latexEqnModel = "{%5%}\\right\\}";
                        this.getHTML = prepareHTML(null, "&#125;", name);
                    },
                    "ANGLE_LEFT": function() {
                        var name = "ANGLE_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left\\langle{%5%}";
                        this.getHTML = prepareHTML("&#9001;", null, name);
                    },
                    "ANGLE_RIGHT": function() {
                        var name = "ANGLE_RIGHT";
                        this.name = name;
                        this.latexEqnModel = "{%5%}\\right\\rangle";
                        this.getHTML = prepareHTML(null, "&#9002;", name);
                    },
                    "LINE_LEFT": function() {
                        var name = "LINE_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left|{%5%}";
                        this.getHTML = prepareHTML("&#124;", null, name);
                    },
                    "LINE_RIGHT": function() {
                        var name = "LINE_RIGHT";
                        this.name = name;
                        this.latexEqnModel = "{%5%}\\right|";
                        this.getHTML = prepareHTML(null, "&#124;", name);
                    },
                    "DOUBLE_LINE_LEFT": function() {
                        var name = "DOUBLE_LINE_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left\\|{%5%}";
                        this.getHTML = prepareHTML("&#8214;", null, name);
                    },
                    "DOUBLE_LINE_RIGHT": function() {
                        var name = "DOUBLE_LINE_RIGHT";
                        this.name = name;
                        this.latexEqnModel = "{%5%}\\right\\|";
                        this.getHTML = prepareHTML(null, "&#8214;", name);
                    },
                    "DOUBLE_SQUARE_LEFT": function() {
                        var name = "DOUBLE_SQUARE_LEFT";
                        this.name = name;
                        this.latexEqnModel = "\\left[\\kern-0.15em\\left[{%5%}";
                        //****unicode not found,so using simple square
                        this.getHTML = prepareHTML("&#91;", null, name);
                    },
                    "DOUBLE_SQUARE_RIGHT": function() {
                        var name = "DOUBLE_SQUARE_RIGHT";
                        this.name = name;
                        //****unicode not found,so using simple square
                        this.latexEqnModel = "{%5%}\\right]\\kern-0.15em\\right]";
                        this.getHTML = prepareHTML(null, "&#93;", name);
                    },
                };
                this.variants = variants;
                var onChangeCallback = function(changeParams, feEqnParent) {
                    if (changeParams.changeType !== "TYPING") {
                        var icon = feEqnParent.children(".feIconBRACES");

                        var vertScale = icon.siblings(".feCoreBRACES").outerHeight(true) / icon.height();
                        icon.css(familyObj.getCSSTransformAttrs("1," + vertScale));
                    }
                };
            },
            FRAC_ROOT: function() {
                var familyObj = $.extend(this, new htmlUtilities());
                var familyName = "FRAC_ROOT";
                this.familyName = familyName;
                var division = "DIVISION"
                var divisionWithQuotient = "DIVISION_WITH_QUOTIENT";
                var prepareFracHTML = function(subFamilyName, onChangeCallBack) {
                    var eqnContentDiv = makeHTMLTag("div");
                    //putting 2 and 8 for numerator and denominator
                    //adding extra classes for navigation
                    //.data("navigationPtrClass", "feCore");
                    var ul = familyObj.getPH(null, 2).toggleClass("feEqnFRAC_ROOT_UL feUL feCore");
                    var ll = familyObj.getPH(null, 8).toggleClass("feEqnFRAC_ROOT_LL feLL feCore");
                    ul.data("navigationPtrClass", "feUL");
                    ll.data("navigationPtrClass", "feLL");
                    eqnContentDiv.append(ul);
                    eqnContentDiv.append(ll);
                    eqnContentDiv.append(makeHTMLTag("div", {"class": "alignerBlockBalancer"}));
                    if (onChangeCallBack) {
                        eqnContentDiv.data("onChangeCallback", onChangeCallback);
                    }
                    familyObj.commonNavigationRules(eqnContentDiv);
                    var wrappedEqn = familyObj.wrapInFeEqnParent(eqnContentDiv, familyName, subFamilyName);
                    return wrappedEqn;
                };
                var prepareSqrtHTML = function(iconValue, subFamilyName, onChangeCallBack, hasExtras) {
                    var eqnContentDiv = makeHTMLTag("div");
                    var alignerBlock = makeHTMLTag("div", {class: "alignerBlock"});
                    if (subFamilyName === divisionWithQuotient || subFamilyName === division) {
                        if (hasExtras) {
                            var ul = familyObj.getULSmallPH(null, 2);
                            alignerBlock.append(ul);
                            alignerBlock.append(makeHTMLTag("div", {"class": "alignerBlockBalancer"}));
                            eqnContentDiv.append(alignerBlock);
                        }
                        var newDiv = makeHTMLTag("div");
                        newDiv.append(familyObj.getIcon(iconValue));
                        newDiv.append(familyObj.getPH(null, 5));
                        eqnContentDiv.append(newDiv);

                    } else {
                        if (hasExtras) {
                            alignerBlock.append(familyObj.getULSmallPH(null, 2));
                            alignerBlock.append(makeHTMLTag("div", {"class": "alignerBlockBalancer"}));
                            eqnContentDiv.append(alignerBlock);
                        }
                        var newDiv = makeHTMLTag("div", {"class": "linBlk"});
                        newDiv.append(familyObj.getIcon(iconValue));
                        newDiv.append(familyObj.getPH(null, 5));
                        eqnContentDiv.append(newDiv);
                    }
                    if (onChangeCallBack) {
                        eqnContentDiv.data("onChangeCallback", onChangeCallback);
                    }
                    familyObj.commonNavigationRules(eqnContentDiv);
                    var wrappedEqn = familyObj.wrapInFeEqnParent(eqnContentDiv, familyName, subFamilyName);
                    return wrappedEqn;
                };

                var variants = {
                    "FRACTION": function() {
                        var name = "FRACTION";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\frac { %2% }{ %8% }";
                        this.getHTML = prepareFracHTML(name);
                    },
                    "SQUARE_ROOT": function() {
                        var name = "SQUARE_ROOT";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\sqrt{%5%}";
                        this.getHTML = prepareSqrtHTML("&#8730;", name, onChangeCallback);
                    },
                    "ANY_ROOT": function() {
                        var name = "ANY_ROOT";
                        this.name = name;
                        this.latexEqnModel = "\\sqrt[%2%]{%5%}";
                        this.getHTML = prepareSqrtHTML("&#8730;", name, onChangeCallback, true);
                    },
                    "DIVISION": function() {
                        var name = "DIVISION";
                        this.name = name;
                        this.latexEqnModel = "\\left){\\vphantom{1{%5%}}}\\right.\\!\\!\\!\\!\\overline{\\,\\,\\,\\vphantom 1{{%5%}}}";
                        this.getHTML = prepareSqrtHTML("&#41;", name, onChangeCallback, false);
                    },
                    "DIVISION_WITH_QUOTIENT": function() {
                        var name = "DIVISION_WITH_QUOTIENT";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{\\left){\\vphantom{1{%5%}}}\\right.\\!\\!\\!\\!\\overline{\\,\\,\\,\\vphantom 1{{%5%}}}}\\limits^{\\displaystyle\\hfill\\,\\,\\, {%2%}}";
                        this.getHTML = prepareSqrtHTML("&#41;", name, onChangeCallback, true);
                    }
                };
                this.variants = variants;
                var onChangeCallback = function(changeParams, feEqnParent) {
                    if (changeParams.changeType !== "TYPING") {
                        var feIcon = feEqnParent.find(".feIconFRAC_ROOT")
                                .not(feEqnParent.find(".feEqnParent").find(".feIconFRAC_ROOT"));
                        var feCoreFracRoot = feEqnParent.find(".feCoreFRAC_ROOT")
                                .not(feEqnParent.find(".feEqnParent").find(".feCoreFRAC_ROOT"));
                        var vertScale = feCoreFracRoot.outerHeight(true) / feIcon.height();
                        var subFamily = feEqnParent.data().subFamily;
                        if (subFamily === division || subFamily === divisionWithQuotient) {
                            vertScale += (vertScale / 5);//adjustment
                        }
                        feIcon.css(familyObj.getCSSTransformAttrs("1," + vertScale));
                    }
                };
            },
            EX: function() {
                var familyObj = $.extend(this, new htmlUtilities());
                var familyName = "EX";
                this.familyName = familyName;
                var topBottomLimitsStr = "EX_TOP_BOTTOM_LIMITS";
                var adjacentLimitsStr = "EX_ADJACENT_LIMITS";
                var setNavigationRules = function(feEqnParent, hasAdjacentPowers) {
                    var commonRule = ["feUL", "feCore", "feLL"];
                    var nextPrevRule = commonRule;
                    if (hasAdjacentPowers) {
                        nextPrevRule = ["feCore", "feUL", "feLL"];
                    }
                    var upDownRule = commonRule;
                    feEqnParent.data("nextPrevRule", nextPrevRule).data("upDownRule", upDownRule);
                };
                var prepareHTML = function(subFamilyName, extraNameSpaces,
                        hasPower, hasBase) {
                    if (extraNameSpaces.split(",").indexOf(adjacentLimitsStr) > -1) {
                        var eqnContentDiv = makeHTMLTag("div");
                        eqnContentDiv.append(familyObj.getPH(null, 5));
                        var alignerBlock = makeHTMLTag("div", {class: "alignerBlock"});
                        alignerBlock.append(makeHTMLTag("div", {"class": "alignerBlockBalancer"}));

                        if (hasPower) {
                            alignerBlock.prepend(familyObj.getULSmallPH(null, 2));
                        }
                        if (hasBase) {
                            alignerBlock.append(familyObj.getLLSmallPH(null, 8));
                        }
                        eqnContentDiv.append(alignerBlock);
                        setNavigationRules(eqnContentDiv, true);
                    } else {
                        var eqnContentDiv = makeHTMLTag("div", {class: "alignerBlock"});
                        if (extraNameSpaces.split(",").indexOf(adjacentLimitsStr) > -1) {
                            setNavigationRules(eqnContentDiv, true);
                        } else {
                            setNavigationRules(eqnContentDiv, false);
                        }
                        if (hasPower) {
                            eqnContentDiv.append(familyObj.getULSmallPH(null, 2));
                        }
                        eqnContentDiv.append(familyObj.getPH(null, 5));
                        if (hasBase) {
                            eqnContentDiv.append(familyObj.getLLSmallPH(null, 8));
                        }
                        setNavigationRules(eqnContentDiv, false);
                    }
                    eqnContentDiv.addClass("feExtendersPa").data("onChangeCallback", onChangeCallback);
                    var wrappedEqn = familyObj.wrapInFeEqnParent(eqnContentDiv, familyName, subFamilyName, null, extraNameSpaces);
                    return wrappedEqn;
                };

                var variants = {
                    "POWER_TOP": function() {
                        var name = "POWER_TOP";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{%5%}\\limits^{%2%}";
                        this.getHTML = prepareHTML(name, topBottomLimitsStr,
                                true, false);
                    },
                    "BASE_BOTTOM": function() {
                        var name = "BASE_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{%5%}\\limits_{%8%}";
                        this.getHTML = prepareHTML(name, topBottomLimitsStr,
                                false, true);
                    },
                    "POWER_TOP_BASE_BOTTOM": function() {
                        var name = "POWER_TOP_BASE_BOTTOM";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\mathop{%5%}\\limits_{%8%}^{%2%}";
                        this.getHTML = prepareHTML(name, topBottomLimitsStr,
                                true, true);
                    },
                    "POWER": function() {
                        var name = "POWER";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{%5%}\\nolimits^{%2%}";
                        this.getHTML = prepareHTML(name, adjacentLimitsStr,
                                true, false);
                    },
                    "BASE": function() {
                        var name = "BASE";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{%5%}\\nolimits_{%8%}";
                        this.getHTML = prepareHTML(name, adjacentLimitsStr,
                                false, true);
                    },
                    "POWER_BASE": function() {
                        var name = "POWER_BASE";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\mathop{%5%}\\nolimits_{%8%}^{%2%}";
                        this.getHTML = prepareHTML(name, adjacentLimitsStr,
                                true, true);
                    }
                };
                this.variants = variants;
                var onChangeCallback = function(changeParams, feEqnParent) {
                    if (changeParams.changeType !== "TYPING" && feEqnParent.hasClass("feEqnParentEX_ADJACENT_LIMITS")) {
                        var alignerBlock = feEqnParent.children(".alignerBlock");
                        var ul = alignerBlock.children(".feUL");
                        var ll = alignerBlock.children(".feLL");
                        var h = feEqnParent.children(".feCoreEX").outerHeight(true) / 2;
                        if (ul.length > 0) {
                            var fontSize = parseFloat(ul.css('font-size')) * 1;
                            ul.css("margin-bottom", (h - fontSize));
                        }
                        if (ll.length > 0) {
                            var fontSize = parseFloat(ll.css('font-size')) * 0.75;
                            ll.css("margin-top", (h - fontSize));
                        }
                    }
                };
                var alignFeExtPa = function(feEqnParent) {
                    if (!feEqnParent || feEqnParent.length === 0)
                        return;
                    var botDelta = getFeExtenderDelta("feBotExtender", feEqnParent);
                    var topDelta = getFeExtenderDelta("feTopExtender", feEqnParent);
                    var diff = (botDelta - topDelta) / 2;
                    feEqnParent.css("top", diff);
                    //alignFeExtenders(feEqnParent);
                };
                var getFeExtenderDelta = function(className, feExtPa) {
                    var someEl = feExtPa.find("." + className).not(feExtPa.find(".feEqnParent")
                            .find(".feExtenders"));
                    var someDelta = 0;
                    if (someEl.length > 0) {
                        someDelta = someEl.outerHeight(true);
                    }
                    return someDelta;
                };
            },
            SIGMA: function() {
                $.extend(this, new htmlUtilities());
                var familyName = "SIGMA";
                this.familyName = familyName;
                var integralRelatedFnObj = new this.integralRelatedEqns(familyName);
                var prepareHTML = integralRelatedFnObj.prepareHTML;
                var topBottomLimitsStr = "SIGMA_TOP_BOTTOM_LIMITS";
                var adjacentLimitsStr = "SIGMA_ADJACENT_LIMITS";
                var sigmaUnicode = "&#931;";


                var variants = {
                    "NO_LIMITS": function() {
                        var name = "NO_LIMITS";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\sum{%5%}";
                        this.getHTML = prepareHTML(sigmaUnicode, name, null,
                                false, false);
                    },
                    "BOTTOM_LIMIT": function() {
                        var name = "BOTTOM_LIMIT";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\sum\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML(sigmaUnicode, name, topBottomLimitsStr,
                                false, true);
                    },
                    "TOP_BOTTOM_LIMITS": function() {
                        var name = "TOP_BOTTOM_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\sum\\limits_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(sigmaUnicode, name, topBottomLimitsStr,
                                true, true);
                    },
                    "ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\sum_{%8%}{%5%}";
                        this.getHTML = prepareHTML(sigmaUnicode, name, adjacentLimitsStr,
                                false, true);
                    },
                    "ADJACENT_LIMITS": function() {
                        var name = "ADJACENT_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\sum_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(sigmaUnicode, name, adjacentLimitsStr,
                                true, true);
                    }
                };
                this.variants = variants;
            },
            INTEGRAL: function() {
                $.extend(this, new htmlUtilities());
                var familyName = "INTEGRAL";
                this.familyName = familyName;
                var integralRelatedFnObj = new this.integralRelatedEqns(familyName);
                var prepareHTML = integralRelatedFnObj.prepareHTML;
                var topBottomLimitsStr = "INTEGRAL_TOP_BOTTOM_LIMITS";
                var adjacentLimitsStr = "INTEGRAL_ADJACENT_LIMITS";
                var variants = {
                    "SINGLE_NO_LIMITS": function() {
                        var name = "SINGLE_NO_LIMITS";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\int{%5%}";
                        this.getHTML = prepareHTML("&#8747;", name, null,
                                false, false);
                    },
                    "SINGLE_WITH_TOP_LIMITS": function() {
                        var name = "SINGLE_WITH_TOP_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\int\\limits_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML("&#8747;", name, topBottomLimitsStr,
                                true, true);
                    },
                    "SINGLE_WITH_ADJACENT_LIMITS": function() {
                        var name = "SINGLE_WITH_ADJACENT_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\int_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML("&#8747;", name, adjacentLimitsStr,
                                true, true);
                    },
                    "SINGLE_NO_LIMITS_2": function() {
                        var name = "SINGLE_NO_LIMITS_2";
                        this.name = name;
                        this.latexEqnModel = "\\int{%5%}";
                        this.getHTML = prepareHTML("&#8747;", name, null,
                                false, false);
                    },
                    "SINGLE_WITH_BOTTOM_LIMIT": function() {
                        var name = "SINGLE_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\int\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8747;", name, topBottomLimitsStr,
                                false, true);
                    },
                    "SINGLE_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "SINGLE_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\int_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8747;", name, adjacentLimitsStr,
                                false, true);
                    },
                    "DOUBLE_NO_LIMITS": function() {
                        var name = "DOUBLE_NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\iint{%5%}";
                        this.getHTML = prepareHTML("&#8748;", name, null,
                                false, false);
                    },
                    "DOUBLE_WITH_BOTTOM_LIMIT": function() {
                        var name = "DOUBLE_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\iint\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8748;", name, topBottomLimitsStr,
                                false, true);
                    },
                    "DOUBLE_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "DOUBLE_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\iint_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8748;", name, adjacentLimitsStr,
                                false, true);
                    },
                    "TRIPLE_NO_LIMITS": function() {
                        var name = "TRIPLE_NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\iiint{%5%}";
                        this.getHTML = prepareHTML("&#8749;", name, null,
                                false, false);
                    },
                    "TRIPLE_WITH_BOTTOM_LIMIT": function() {
                        var name = "TRIPLE_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\iiint\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8749;", name, topBottomLimitsStr,
                                false, true);
                    },
                    "TRIPLE_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "TRIPLE_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\iiint_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8749;", name, adjacentLimitsStr,
                                false, true);
                    },
                    "SINGLE_CIRCULAR_NO_LIMITS": function() {
                        var name = "SINGLE_CIRCULAR_NO_LIMITS";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\oint{%5%}";
                        this.getHTML = prepareHTML("&#8750;", name, null,
                                false, false);
                    },
                    "SINGLE_CIRCULAR_WITH_BOTTOM_LIMIT": function() {
                        var name = "SINGLE_CIRCULAR_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\oint\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8750;", name, topBottomLimitsStr,
                                false, true);
                    },
                    "SINGLE_CIRCULAR_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "SINGLE_CIRCULAR_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\oint_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8750;", name, adjacentLimitsStr,
                                false, true);
                    },
                    "DOUBLE_CIRCULAR_NO_LIMITS": function() {
                        var name = "DOUBLE_CIRCULAR_NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{{\\int\\!\\!\\!\\!\\!\\int}\\mkern-21mu \\bigcirc}{%5%}";
                        this.getHTML = prepareHTML("&#8751;", name, null,
                                false, false);
                    },
                    "DOUBLE_CIRCULAR_WITH_BOTTOM_LIMIT": function() {
                        var name = "DOUBLE_CIRCULAR_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{{\\int\\!\\!\\!\\!\\!\\int}\\mkern-21mu \\bigcirc}\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8751;", name, topBottomLimitsStr,
                                false, true);
                    },
                    "DOUBLE_CIRCULAR_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "DOUBLE_CIRCULAR_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{{\\int\\!\\!\\!\\!\\!\\int}\\mkern-21mu \\bigcirc}_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8751;", name, adjacentLimitsStr,
                                false, true);
                    },
                    "TRIPLE_CIRCULAR_NO_LIMITS": function() {
                        var name = "TRIPLE_CIRCULAR_NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{{\\int\\!\\!\\!\\!\\!\\int\\!\\!\\!\\!\\!\\int}\\mkern-31.2mu \\bigodot}{%5%}";
                        this.getHTML = prepareHTML("&#8752;", name, null,
                                false, false);
                    },
                    "TRIPLE_CIRCULAR_WITH_BOTTOM_LIMIT": function() {
                        var name = "TRIPLE_CIRCULAR_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{{\\int\\!\\!\\!\\!\\!\\int\\!\\!\\!\\!\\!\\int}\\mkern-31.2mu \\bigodot}\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8752;", name, topBottomLimitsStr,
                                false, true);
                    },
                    "TRIPLE_CIRCULAR_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "TRIPLE_CIRCULAR_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\mathop{{\\int\\!\\!\\!\\!\\!\\int\\!\\!\\!\\!\\!\\int}\\mkern-31.2mu \\bigodot}_{%8%}{%5%}";
                        this.getHTML = prepareHTML("&#8752;", name, adjacentLimitsStr,
                                false, true);
                    }
                };
                this.variants = variants;
            },
            ARROWS: function() {
                var familyObj = $.extend(this, new htmlUtilities());
                var familyName = "ARROWS";
                this.familyName = familyName;
                var setNavigationRules = function(feEqnParent) {
                    var nextPrevRule = ["feUL", "feLL", "feCore"];
                    var upDownRule = ["feUL", "feCore", "feLL"];
                    feEqnParent.data("nextPrevRule", nextPrevRule).data("upDownRule", upDownRule);
                };
                var prepareHTML = function(iconValue, subFamilyName, hasTopLimit, hasBottomLimit) {
                    var eqnContentDiv = makeHTMLTag("div", {class: "alignerBlock"});
                    if (hasTopLimit) {
                        eqnContentDiv.append(familyObj.getULSmallPH(null, 2));
                    }
                    eqnContentDiv.append(familyObj.getIcon(iconValue));
                    if (hasBottomLimit) {
                        eqnContentDiv.append(familyObj.getLLSmallPH(null, 8));
                    }
                    eqnContentDiv.data("onChangeCallback", onChangeCallback);
                    familyObj.commonNavigationRules(eqnContentDiv);
                    var wrappedEqn = familyObj.wrapInFeEqnParent(eqnContentDiv, familyName, subFamilyName);
                    return wrappedEqn;
                };
                var rightArrow = "&#8594;";
                var leftArrow = "&#8592;";
                var leftRightArrow = "&#8596;";
                var rightOverLeftArrow = "&#8644;";
                var leftOverRightArrow = "&#8646;";
                var reversibleArrow = "&#8652;";
                //arrows unicodes source: http://www.alanwood.net/unicode/arrows.html
                var variants = {
                    "RIGHT_ARROW_TOP": function() {
                        var name = "RIGHT_ARROW_TOP";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\longrightarrow{{%2%}}";
                        this.getHTML = prepareHTML(rightArrow, name, true, false);
                    },
                    "RIGHT_ARROW_BOTTOM": function() {
                        var name = "RIGHT_ARROW_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\longrightarrow[{%8%}]{}";
                        this.getHTML = prepareHTML(rightArrow, name, false, true);
                    },
                    "RIGHT_ARROW_TOP_BOTTOM": function() {
                        var name = "RIGHT_ARROW_TOP_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\longrightarrow[{%8%}]{{%2%}}";
                        this.getHTML = prepareHTML(rightArrow, name, true, true);
                    },
                    "LEFT_ARROW_TOP": function() {
                        var name = "LEFT_ARROW_TOP";
                        this.name = name;
                        this.latexEqnModel = "\\longleftarrow{{%2%}}";
                        this.getHTML = prepareHTML(leftArrow, name, true, false);
                    },
                    "LEFT_ARROW_BOTTOM": function() {
                        var name = "LEFT_ARROW_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\longleftarrow[{%8%}]{}";
                        this.getHTML = prepareHTML(leftArrow, name, false, true);
                    },
                    "LEFT_ARROW_TOP_BOTTOM": function() {
                        var name = "LEFT_ARROW_TOP_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\longleftarrow[{%8%}]{{%2%}}";
                        this.getHTML = prepareHTML(leftArrow, name, true, true);
                    },
                    "LEFT_RIGHT_ARROW_TOP": function() {
                        var name = "LEFT_RIGHT_ARROW_TOP";
                        this.name = name;
                        this.latexEqnModel = "\\overset{%2%} \\longleftrightarrow";
                        this.getHTML = prepareHTML(leftRightArrow, name, true, false);
                    },
                    "LEFT_RIGHT_ARROW_BOTTOM": function() {
                        var name = "LEFT_RIGHT_ARROW_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{%8%} \\longleftrightarrow";
                        this.getHTML = prepareHTML(leftRightArrow, name, false, true);
                    },
                    "LEFT_RIGHT_ARROW_TOP_BOTTOM": function() {
                        var name = "LEFT_RIGHT_ARROW_TOP_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{{%8%}}{\\overset{{%2%}}{\\longleftrightarrow}}";
                        this.getHTML = prepareHTML(leftRightArrow, name, true, true);
                    },
                    "RIGHT_OVER_LEFT_ARROW_TOP": function() {
                        var name = "RIGHT_OVER_LEFT_ARROW_TOP";
                        this.name = name;
                        this.latexEqnModel = "\\overset{%2%} \\rightleftarrows";
                        this.getHTML = prepareHTML(rightOverLeftArrow, name, true, false);
                    },
                    "RIGHT_OVER_LEFT_ARROW_BOTTOM": function() {
                        var name = "RIGHT_OVER_LEFT_ARROW_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{%8%} \\rightleftarrows";
                        this.getHTML = prepareHTML(rightOverLeftArrow, name, false, true);
                    },
                    "RIGHT_OVER_LEFT_ARROW_TOP_BOTTOM": function() {
                        var name = "RIGHT_OVER_LEFT_ARROW_TOP_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{{%8%}}{\\overset{{%2%}}{\\rightleftarrows}}";
                        this.getHTML = prepareHTML(rightOverLeftArrow, name, true, true);
                    },
                    "LEFT_OVER_RIGHT_ARROW_TOP": function() {
                        var name = "LEFT_OVER_RIGHT_ARROW_TOP";
                        this.name = name;
                        this.latexEqnModel = "\\overset{%2%} \\leftrightarrows";
                        this.getHTML = prepareHTML(leftOverRightArrow, name, true, false);
                    },
                    "LEFT_OVER_RIGHT_ARROW_BOTTOM": function() {
                        var name = "LEFT_OVER_RIGHT_ARROW_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{%8%} \\leftrightarrows";
                        this.getHTML = prepareHTML(leftOverRightArrow, name, false, true);
                    },
                    "LEFT_OVER_RIGHT_ARROW_TOP_BOTTOM": function() {
                        var name = "LEFT_OVER_RIGHT_ARROW_TOP_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{{%8%}}{\\overset{{%2%}}{\\leftrightarrows}}";
                        this.getHTML = prepareHTML(leftOverRightArrow, name, true, true);
                    },
                    "REVERSIBLE_ARROW_TOP": function() {
                        var name = "REVERSIBLE_ARROW_TOP";
                        this.name = name;
                        this.latexEqnModel = "\\overset{%2%} \\rightleftharpoons";
                        this.getHTML = prepareHTML(reversibleArrow, name, true, false);
                    },
                    "REVERSIBLE_ARROW_BOTTOM": function() {
                        var name = "REVERSIBLE_ARROW_BOTTOM";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\underset{%8%} \\rightleftharpoons";
                        this.getHTML = prepareHTML(reversibleArrow, name, false, true);
                    },
                    "REVERSIBLE_ARROW_TOP_BOTTOM": function() {
                        var name = "REVERSIBLE_ARROW_TOP_BOTTOM";
                        this.name = name;
                        this.latexEqnModel = "\\underset{{%8%}}{\\overset{{%2%}}{\\rightleftharpoons}}";
                        this.getHTML = prepareHTML(reversibleArrow, name, true, true);
                    }
                };
                this.variants = variants;
                var onChangeCallback = function(changeParams, feEqnParent) {
                    var feIcon = feEqnParent.children(".feIconARROWS");
                    var horiScale = feEqnParent.width() / feIcon.width();
                    feIcon.css(familyObj.getCSSTransformAttrs(horiScale + ",1"));
                };
            },
            CAP: function() {
                $.extend(this, new htmlUtilities());
                var familyName = "CAP";
                this.familyName = familyName;
                var integralRelatedFnObj = new this.integralRelatedEqns(familyName);
                var prepareHTML = integralRelatedFnObj.prepareHTML;
                var topBottomLimitsStr = "CAP_TOP_BOTTOM_LIMITS";
                var adjacentLimitsStr = "CAP_ADJACENT_LIMITS";
                var productUnicode = "&#8719;";
                var coproductUnicode = "&#8720;";
                var intersectionUnicode = "&#8898;";
                var unionUnicode = "&#8899;";
                var variants = {
                    "PRODUCT_NO_LIMITS": function() {
                        var name = "NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\prod{%5%}";
                        this.getHTML = prepareHTML(productUnicode, name, null,
                                false, false);
                    },
                    "PRODUCT_WITH_BOTTOM_LIMIT": function() {
                        var name = "PRODUCT_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\prod\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML(productUnicode, name, topBottomLimitsStr,
                                false, true);
                    },
                    "PRODUCT_WITH_TOP_BOTTOM_LIMITS": function() {
                        var name = "PRODUCT_WITH_TOP_LIMITS";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\prod\\limits_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(productUnicode, name, topBottomLimitsStr,
                                true, true);
                    },
                    "PRODUCT_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "PRODUCT_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\prod_{%8%}{%5%}";
                        this.getHTML = prepareHTML(productUnicode, name, adjacentLimitsStr,
                                false, true);
                    },
                    "PRODUCT_WITH_ADJACENT_LIMITS": function() {
                        var name = "PRODUCT_WITH_ADJACENT_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\prod_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(productUnicode, name, adjacentLimitsStr,
                                true, true);
                    },
                    "COPRODUCT_NO_LIMITS": function() {
                        var name = "NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\coprod{%5%}";
                        this.getHTML = prepareHTML(coproductUnicode, name, null,
                                false, false);
                    },
                    "COPRODUCT_WITH_BOTTOM_LIMIT": function() {
                        var name = "COPRODUCT_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\coprod\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML(coproductUnicode, name, topBottomLimitsStr,
                                false, true);
                    },
                    "COPRODUCT_WITH_TOP_BOTTOM_LIMITS": function() {
                        var name = "COPRODUCT_WITH_TOP_BOTTOM_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\coprod\\limits_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(coproductUnicode, name, topBottomLimitsStr,
                                true, true);
                    },
                    "COPRODUCT_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "COPRODUCT_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\coprod_{%8%}{%5%}";
                        this.getHTML = prepareHTML(coproductUnicode, name, adjacentLimitsStr,
                                false, true);
                    },
                    "COPRODUCT_WITH_ADJACENT_LIMITS": function() {
                        var name = "COPRODUCT_WITH_ADJACENT_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\coprod_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(coproductUnicode, name, adjacentLimitsStr,
                                true, true);
                    },
                    "INTERSECTION_NO_LIMITS": function() {
                        var name = "NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\bigcap{%5%}";
                        this.getHTML = prepareHTML(intersectionUnicode, name, null,
                                false, false);
                    },
                    "INTERSECTION_WITH_BOTTOM_LIMIT": function() {
                        var name = "INTERSECTION_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\bigcap\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML(intersectionUnicode, name, topBottomLimitsStr,
                                false, true);
                    },
                    "INTERSECTION_WITH_TOP_BOTTOM_LIMITS": function() {
                        var name = "INTERSECTION_WITH_TOP_BOTTOM_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\bigcap\\limits_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(intersectionUnicode, name, topBottomLimitsStr,
                                true, true);
                    },
                    "INTERSECTION_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "INTERSECTION_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\bigcap_{%8%}{%5%}";
                        this.getHTML = prepareHTML(intersectionUnicode, name, adjacentLimitsStr,
                                false, true);
                    },
                    "INTERSECTION_WITH_ADJACENT_LIMITS": function() {
                        var name = "INTERSECTION_WITH_ADJACENT_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\bigcap_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(intersectionUnicode, name, adjacentLimitsStr,
                                true, true);
                    },
                    "UNION_NO_LIMITS": function() {
                        var name = "NO_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\bigcup{%5%}";
                        this.getHTML = prepareHTML(unionUnicode, name, null,
                                false, false);
                    },
                    "UNION_WITH_BOTTOM_LIMIT": function() {
                        var name = "UNION_WITH_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\bigcup\\limits_{%8%}{%5%}";
                        this.getHTML = prepareHTML(unionUnicode, name, topBottomLimitsStr,
                                false, true);
                    },
                    "UNION_WITH_TOP_BOTTOM_LIMITS": function() {
                        var name = "UNION_WITH_TOP_BOTTOM_LIMITS";
                        this.name = name;
                        this.isFamilyIcon = true;
                        this.latexEqnModel = "\\bigcup\\limits_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(unionUnicode, name, topBottomLimitsStr,
                                true, true);
                    },
                    "UNION_WITH_ADJACENT_BOTTOM_LIMIT": function() {
                        var name = "UNION_WITH_ADJACENT_BOTTOM_LIMIT";
                        this.name = name;
                        this.latexEqnModel = "\\bigcup_{%8%}{%5%}";
                        this.getHTML = prepareHTML(unionUnicode, name, adjacentLimitsStr,
                                false, true);
                    },
                    "UNION_WITH_ADJACENT_LIMITS": function() {
                        var name = "UNION_WITH_ADJACENT_LIMITS";
                        this.name = name;
                        this.latexEqnModel = "\\bigcup_{%8%}^{%2%}{%5%}";
                        this.getHTML = prepareHTML(unionUnicode, name, adjacentLimitsStr,
                                true, true);
                    }
                };
                this.variants = variants;
            },
        };
        this.feFamilies = feFamilies;
    })($);
})(jQuery);
vFE.init();