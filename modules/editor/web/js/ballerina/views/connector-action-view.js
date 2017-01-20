/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['lodash', 'log', 'd3', 'jquery', 'd3utils', './ballerina-view', './../ast/connector-action',
        './default-worker', './point', './connector-declaration-view', './statement-view-factory',
        'ballerina/ast/ballerina-ast-factory', './expression-view-factory','./message', './statement-container',
        './../ast/variable-declaration', './variables-view', './annotation-view',
        './function-arguments-view', './return-type-view'],
    function (_, log, d3, $, D3utils, BallerinaView, ConnectorAction,
              DefaultWorkerView, Point, ConnectorDeclarationView, StatementViewFactory,
              BallerinaASTFactory, ExpressionViewFactory, MessageView, StatementContainer,
              VariableDeclaration, VariablesView, AnnotationView,
              ArgumentsView, ReturnTypeView) {

        /**
         * The view to represent a connector action which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ConnectorAction} args.model - The connector action model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ConnectorActionView = function (args) {
            BallerinaView.call(this, args);
            this._connectorWorkerViewList = [];
            this._defaultWorker = undefined;
            this._statementExpressionViewList = [];
            // TODO: Instead of using the parentView use the parent. Fix this from BallerinaView.js and bellow
            this._parentView = _.get(args, "parentView");

            if (_.isNil(this._model) || !(this._model instanceof ConnectorAction)) {
                log.error("Connector Action is undefined or is of different type." + this._model);
                throw "Connector Action is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for connector action is undefined." + this._container);
                throw "Container for connector action is undefined." + this._container;
            }

            // Center point of the connector action view
            this._viewOptions.topLeft = _.get(args, "viewOptions.topLeft", new Point(50, 100));
            this._viewOptions.startActionOffSet = _.get(args, "viewOptions.startActionOffSet", 60);

            // Center point of the default worker
            this._viewOptions.defaultWorker = _.get(args, "viewOptions.defaultWorker", {});
            this._viewOptions.defaultWorker.offsetTop = _.get(args, "viewOptions.defaultWorker.offsetTop", 50);
            this._viewOptions.defaultWorker.center = _.get(args, "viewOptions.defaultWorker.centerPoint",
                this._viewOptions.topLeft.clone().move(100, 150));

            // View options for height and width of the heading box.
            this._viewOptions.heading = _.get(args, "viewOptions.heading", {});
            this._viewOptions.heading.height = _.get(args, "viewOptions.heading.height", 25);
            this._viewOptions.heading.width = _.get(args, "viewOptions.heading.width", this._container.node().ownerSVGElement.parentElement.offsetWidth - 100);

            // View options for height and width of the connector action icon in the heading box.
            this._viewOptions.heading.icon = _.get(args, "viewOptions.heading.icon", {});
            this._viewOptions.heading.icon.height = _.get(args, "viewOptions.heading.icon.height", 25);
            this._viewOptions.heading.icon.width = _.get(args, "viewOptions.heading.icon.width", 25);

            this._viewOptions.contentCollapsed = _.get(args, "viewOptions.contentCollapsed", false);
            this._viewOptions.contentWidth = _.get(args, "viewOptions.contentWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 100);
            this._viewOptions.contentHeight = _.get(args, "viewOptions.contentHeight", 470);
            this._viewOptions.collapseIconWidth = _.get(args, "viewOptions.collaspeIconWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 95);
            this._viewOptions.deleteIconWidth = _.get(args, "viewOptions.deleteIconWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 125);

            this._viewOptions.heading.minWidth = 1000;
            this._viewOptions.contentMinWidth = 1000;

            this._viewOptions.totalHeightGap = 50;
            this._viewOptions.LifeLineCenterGap = 180;
            this._viewOptions.defua = 180;
            this._viewOptions.hoverClass = _.get(args, "viewOptions.cssClass.hover_svg", 'design-view-hover-svg');

            this._variableButton = undefined;
            this._variablePane = undefined;

            //setting initial height for connector action container
            this._totalHeight = 230;
            this._headerIconGroup = undefined;
            // initialize bounding box
            this.getBoundingBox().fromTopLeft(this._viewOptions.topLeft, this._viewOptions.heading.width, this._viewOptions.heading.height
                + this._viewOptions.contentHeight);

            this._connectorActionGroup = undefined;
            this.init();
        };

        ConnectorActionView.prototype = Object.create(BallerinaView.prototype);
        ConnectorActionView.prototype.constructor = ConnectorActionView;
        // TODO move variable types into constant class
        var variableTypes = ['message', 'boolean', 'string', 'int', 'float', 'long', 'double', 'json', 'xml'];

        /**
         * Init the event listeners
         */
        ConnectorActionView.prototype.init = function(){
            this.listenTo(this._model, 'child-removed', this.childViewRemovedCallback);
        };

        /**
         * Get the child container
         * @return {svg} svg container
         */
        ConnectorActionView.prototype.getChildContainer = function () {
            return this._connectorActionGroup;
        };

        ConnectorActionView.prototype.canVisitConnectorAction = function (connectorAction) {
            return true;
        };

        /**
         * Set the connctor acton view's corresponding model
         * @param model
         */
        ConnectorActionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ConnectorAction) {
                this._model = model;
            } else {
                log.error("Connector Action is undefined or is of different type." + model);
                throw "Connector Action is definition undefined or is of different type." + model;
            }
        };

        /**
         * Set the container for the connector action view
         * @param {object} container
         */
        ConnectorActionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for connector action is undefined." + this._container);
                throw "Container for connector action is undefined." + this._container;
            }
        };

        /**
         * set the view Options
         * @param {object} viewOptions
         */
        ConnectorActionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        /**
         * Get the model for the connector action view
         * @return {ConnectorAction} _model
         */
        ConnectorActionView.prototype.getModel = function () {
            return this._model;
        };

        /**
         * Get the container
         * @return {object} _container
         */
        ConnectorActionView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Get the View Options
         * @return {Object} _viewOptions
         */
        ConnectorActionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        ConnectorActionView.prototype.visitStatement = function (statement) {
            var args = {model: statement, container: this._contentGroup.node(), viewOptions: {},
                toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};
            this._statementContainer.renderStatement(statement, args);
        };

        /**
         * Rendering the view for connector action.
         * @returns {group} The svg group which contains the elements of the connector action view.
         */
        ConnectorActionView.prototype.render = function (diagramRenderingContext) {
            this.diagramRenderingContext = diagramRenderingContext;
            var svgContainer = $(this._container)[0];
            var self = this;

            var headingStart = new Point(this._viewOptions.topLeft.x(), this._viewOptions.topLeft.y());
            var contentStart = new Point(this._viewOptions.topLeft.x(),
                this._viewOptions.topLeft.y() + this._viewOptions.heading.height);
            //Main container for a connector action
            var connectorActionGroup = D3utils.group(svgContainer);
            this._connectorActionGroup = connectorActionGroup;
            connectorActionGroup.attr("id", "_" +this._model.id);
            connectorActionGroup.attr("width", this._viewOptions.heading.width)
                .attr("height", this._viewOptions.heading.height + this._viewOptions.contentHeight);
            connectorActionGroup.attr("x", headingStart.x()).attr("y", contentStart.y());

            // Creating SVG definitions group for icons.
            var def = connectorActionGroup.append("defs");
            var iconSizeSideLength = 14;

            // Creating collapsed icon for SVG definitions.
            var collapsedIconSVGPattern = def.append("pattern").attr("id", "collapsedIcon").attr("width", "100%")
                .attr("height", "100%");
            collapsedIconSVGPattern.append("image").attr("xlink:href", "images/down.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var expandIconSVGPattern = def.append("pattern").attr("id", "expandIcon").attr("width", "100%")
                .attr("height", "100%");
            expandIconSVGPattern.append("image").attr("xlink:href", "images/up.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating connector action icon for SVG definitions.
            var connectorActionIconSVGPattern = def.append("pattern").attr("id", "connectorActionIcon").attr("width", "100%")
                .attr("height", "100%");
            // TODO: Change the icon for the connector action
            connectorActionIconSVGPattern.append("image").attr("xlink:href", "images/resource.svg").attr("x", 5)
                .attr("y", 5).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating delete icon for SVG definitions.
            var deleteIconSVGPattern = def.append("pattern").attr("id", "deleteIcon").attr("width", "100%")
                .attr("height", "100%");
            deleteIconSVGPattern.append("image").attr("xlink:href", "images/delete.svg").attr("x", 0).attr("y", 0)
                .attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var deleteRedIconSVGPattern = def.append("pattern").attr("id", "deleteRedIcon").attr("width", "100%")
                .attr("height", "100%");
            deleteRedIconSVGPattern.append("image").attr("xlink:href", "images/delete-red.svg").attr("x", 0).attr("y", 0)
                .attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating annotations icon for SVG definitions.
            var annotationIconSVGPattern = def.append("pattern").attr("id", "annotationIcon").attr("width", "100%")
                .attr("height", "100%");
            annotationIconSVGPattern.append("image").attr("xlink:href", "images/annotation.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var annotationBlackIconSVGPattern = def.append("pattern").attr("id", "annotationBlackIcon").attr("width", "100%")
                .attr("height", "100%");
            annotationBlackIconSVGPattern.append("image").attr("xlink:href", "images/annotation-black.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating arguments icon for SVG definitions.
            var argumentsIconSVGPattern = def.append("pattern").attr("id", "argumentsIcon").attr("width", "100%")
                .attr("height", "100%");
            argumentsIconSVGPattern.append("image").attr("xlink:href", "images/import.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var argumentsBlackIconSVGPattern = def.append("pattern").attr("id", "argumentsBlackIcon").attr("width", "100%")
                .attr("height", "100%");
            argumentsBlackIconSVGPattern.append("image").attr("xlink:href", "images/import-black.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating return type icon for SVG definitions.
            var returnTypeIconSVGPattern = def.append("pattern").attr("id", "returnTypeIcon").attr("width", "100%")
                .attr("height", "100%");
            returnTypeIconSVGPattern.append("image").attr("xlink:href", "images/export.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var returnTypeBlackIconSVGPattern = def.append("pattern").attr("id", "returnTypeBlackIcon").attr("width", "100%")
                .attr("height", "100%");
            returnTypeBlackIconSVGPattern.append("image").attr("xlink:href", "images/export-black.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Connector action header container
            var headerGroup = D3utils.group(connectorActionGroup);
            headerGroup.attr("id", "headerGroup");

            var headingRect = D3utils.rect(headingStart.x(), headingStart.y(),
                this._viewOptions.heading.width, this._viewOptions.heading.height,
                0, 0, headerGroup).classed("headingRect", true);
            this._headingRect = headingRect;

            var headingIconsGroup = D3utils.group(headerGroup);
            headingIconsGroup.attr("transform", "translate(0,0)");
            this._headerIconGroup = headingIconsGroup;

            // Drawing connector action icon
            var headingRectIconHolder = D3utils.rect(headingStart.x(),
                headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("connectorActionHeadingIconHolder",true);

            var headingRectIcon = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("headingRectIcon", true);

            var xEndOfHeadingRect = parseFloat(headingRect.attr("x")) + parseFloat(headingRect.attr("width")) ;
            var yForIcons = parseFloat(headingRect.attr("y")) + (((this._viewOptions.heading.icon.height) / 2) - (14 / 2));

            // Creating wrapper for collpase icon.
            var headingCollapseIconWrapper = D3utils.rect(
                xEndOfHeadingRect - this._viewOptions.heading.icon.width, headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 1, 0, 0, headingIconsGroup)
                .classed("heading-icon-wrapper hoverable heading-icon-collpase-wrapper", true);

            var xForCollpaseIcon = xEndOfHeadingRect - this._viewOptions.heading.icon.width + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Creating connector action heading collapse icon.
            var headingCollapseIcon = D3utils.rect(xForCollpaseIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup)
                .classed("headingCollapsedIcon", true);

            // Creating separator for collapse icon.
            D3utils.line(xEndOfHeadingRect - this._viewOptions.heading.icon.width, parseFloat(headingRect.attr("y")) + 5,
                xEndOfHeadingRect - this._viewOptions.heading.icon.width,
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
                .classed("operations-separator", true);

            // Creating separator for delete icon.
            D3utils.line(xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
                .classed("operations-separator", true);

            // Creating separator for annotation icon.
            D3utils.line(xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
                .classed("operations-separator", true);

            // Creating separator for annotation icon.
            D3utils.line(xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
                .classed("operations-separator", true);

            // Creating wrapper for delete icon.
            var headingDeleteIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
                .classed("heading-icon-wrapper heading-icon-delete-wrapper", true);

            var xForDeleteIcon = xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Connector Action heading delete icon
            var headingDeleteIcon = D3utils.rect(xForDeleteIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).classed("headingDeleteIcon", true);

            // Creating wrapper for annotation icon.
            var headingAnnotationIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
                .classed("heading-icon-wrapper heading-icon-annotation-wrapper", true);

            var xForAnnotationIcon = xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Connector Action heading annotation icon
            var headingAnnotationIcon = D3utils.rect(xForAnnotationIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).classed("headingAnnotationBlackIcon", true);

            // Creating wrapper for arguments icon.
            var headingArgumentsIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
                .classed("heading-icon-wrapper heading-icon-arguments-wrapper", true);

            var xForArgumentsIcon = xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Connector Action heading arguments icon.
            var headingArgumentsIcon = D3utils.rect(xForArgumentsIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).classed("headingArgumentsBlackIcon", true);

            // Creating wrapper for Return Types icon.
            var headingReturnTypesIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
                .classed("heading-icon-wrapper heading-icon-return-type-wrapper", true);

            var xForReturnTypesIcon = xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Connector Action heading Return Types icon.
            var headingReturnTypesIcon = D3utils.rect(xForReturnTypesIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).classed("headingReturnTypeBlackIcon", true);

            // UI changes when the annotation button is clicked.
            $(headingAnnotationIcon.node()).click(function () {
                if ($(this).data("showing") === "true") {
                    $(this).data("showing", "false");
                    headingAnnotationIcon.classed("headingAnnotationBlackIcon", true);
                    headingAnnotationIcon.classed("headingAnnotationIcon", false);
                    headingAnnotationIconWrapper.classed("heading-icon-annotation-wrapper-clicked", false);
                } else {
                    $(this).data("showing", "true");
                    headingAnnotationIcon.classed("headingAnnotationBlackIcon", false);
                    headingAnnotationIcon.classed("headingAnnotationIcon", true);
                    headingAnnotationIconWrapper.classed("heading-icon-annotation-wrapper-clicked", true);
                }
            });

            // UI changes when the arguments button is clicked.
            $(headingArgumentsIcon.node()).click(function () {
                if ($(this).data("showing") === "true") {
                    $(this).data("showing", "false");
                    headingArgumentsIcon.classed("headingArgumentsBlackIcon", true);
                    headingArgumentsIcon.classed("headingArgumentsIcon", false);
                    headingArgumentsIconWrapper.classed("heading-icon-arguments-wrapper-clicked", false);
                } else {
                    $(this).data("showing", "true");
                    headingArgumentsIcon.classed("headingArgumentsBlackIcon", false);
                    headingArgumentsIcon.classed("headingArgumentsIcon", true);
                    headingArgumentsIconWrapper.classed("heading-icon-arguments-wrapper-clicked", true);
                }
            });

            // UI changes when the return Types button is clicked.
            $(headingReturnTypesIcon.node()).click(function () {
                if ($(this).data("showing") === "true") {
                    $(this).data("showing", "false");
                    headingReturnTypesIcon.classed("headingReturnTypeBlackIcon", true);
                    headingReturnTypesIcon.classed("headingReturnTypeIcon", false);
                    headingReturnTypesIconWrapper.classed("heading-icon-return-type-wrapper-clicked", false);
                } else {
                    $(this).data("showing", "true");
                    headingReturnTypesIcon.classed("headingReturnTypeBlackIcon", false);
                    headingReturnTypesIcon.classed("headingReturnTypeIcon", true);
                    headingReturnTypesIconWrapper.classed("heading-icon-return-type-wrapper-clicked", true);
                }
            });

            // Add the connector action name editable html area
            var svgWrappingHtml = this.getChildContainer().node().ownerSVGElement.parentElement;
            var nameDiv = $("<div></div>");
            nameDiv.css('left', (parseInt(headingStart.x()) + 30) + "px");
            nameDiv.css('top', parseInt(headingStart.y()) + "px");
            nameDiv.css('width',"100px");
            nameDiv.css('height',"25px");
            nameDiv.addClass("name-container-div");
            var nameSpan = $("<span></span>");
            nameSpan.text(self._model.getActionName());
            nameSpan.addClass("name-span");
            nameSpan.attr("contenteditable", "true");
            nameSpan.attr("spellcheck", "false");
            nameSpan.focus();
            nameSpan.blur();
            nameDiv.append(nameSpan);
            $(svgWrappingHtml).append(nameDiv);
            // Container for connector action body
            var contentGroup = D3utils.group(connectorActionGroup);
            contentGroup.attr('id', "contentGroup");

            nameSpan.on("change paste keyup", function (e) {
                self._model.setActionName($(this).text());
            }).on("keydown", function (e) {
                // Check whether the Enter key has been pressed. If so return false. Won't type the character
                if (e.keyCode === 13) {
                    return false;
                }
            });

            this._contentGroup = contentGroup;

            var contentRect = D3utils.rect(contentStart.x(), contentStart.y(),
                this._viewOptions.contentWidth, this._viewOptions.contentHeight, 0, 0,
                contentGroup).classed("connector-action-content", true);

            this._contentRect = contentRect;
            contentRect.attr("fill", "#fff");

            var onExpandCollapse = function () {
                var connectorActionBBox = self.getBoundingBox();
                var visibility = contentGroup.node().getAttribute("display");
                if (visibility == "none") {
                    contentGroup.attr("display", "inline");
                    // connector action content is expanded. Hence expand connector action BBox
                    connectorActionBBox.h(connectorActionBBox.h() + self._minizedHeight);

                    // show the variable button and variable pane
                    self._variableButton.show();
                    self._variablePane.show();

                    // Changing icon if the collapse.
                    headingCollapseIcon.classed("headingExpandIcon", false);
                    headingCollapseIcon.classed("headingCollapsedIcon", true);
                }
                else {
                    contentGroup.attr("display", "none");
                    // connector action content is folded. Hence decrease connector action BBox height and keep the minimized size
                    self._minizedHeight =  parseFloat(contentRect.attr('height'));
                    connectorActionBBox.h(connectorActionBBox.h() - self._minizedHeight);

                    // hide the variable button and variable pane
                    self._variableButton.hide();
                    self._variablePane.hide();

                    // Changing icon if the collapse.
                    headingCollapseIcon.classed("headingExpandIcon", true);
                    headingCollapseIcon.classed("headingCollapsedIcon", false);
                }
            };

            // On click of collapse icon hide/show connector action body
            headingCollapseIcon.on("click", onExpandCollapse);
            headingRect.on("click", onExpandCollapse);

            // On click of delete icon
            headingDeleteIcon.on("click", function () {
                log.debug("Clicked delete button");
                var child = self._model;
                var parent = child.parent;
                parent.removeChild(child);
            });

            this.getBoundingBox().on("height-changed", function(dh){
                this._contentRect.attr('height', parseFloat(this._contentRect.attr('height')) + dh);
            }, this);

            this.getBoundingBox().on("right-edge-moved", function(dw){
                var transformX = this._headerIconGroup.node().transform.baseVal.consolidate().matrix.e;
                var transformY = this._headerIconGroup.node().transform.baseVal.consolidate().matrix.f;
                this._headerIconGroup.node().transform.baseVal.getItem(0).setTranslate(transformX + dw, transformY);
                this._contentRect.attr('width', parseFloat(this._contentRect.attr('width')) + dw);
                this._headingRect.attr('width', parseFloat(this._headingRect.attr('width')) + dw);
                // If the bounding box of the connector action go over the svg's current width
                if (this.getBoundingBox().getRight() > this._parentView.getServiceContainer().width()) {
                    this._parentView.setServiceContainerWidth(this.getBoundingBox().getRight() + 60);
                }
            }, this);

            if (_.isUndefined(this._defaultWorker)) {
                var defaultWorkerOpts = {};
                _.set(defaultWorkerOpts, 'container', contentGroup.node());
                _.set(defaultWorkerOpts, 'centerPoint', this._viewOptions.defaultWorker.center);
                _.set(defaultWorkerOpts, 'title', 'ActionWorker');
                this._defaultWorker = new DefaultWorkerView(defaultWorkerOpts);
            }
            this._defaultWorker.render();
            this.trigger("defaultWorkerViewAddedEvent", this._defaultWorker);

            this.initActionLevelDropTarget();
            this.renderStatementContainer();
            log.debug("Rendering Connector Action View");
            this.getModel().accept(this);
            //Removing all the registered 'child-added' event listeners for this model.
            //This is needed because we are not unregistering registered event while the diagram element deletion.
            //Due to that, sometimes we are having two or more view elements listening to the 'child-added' event of same model.
            this._model.off('child-added');
            this._model.on('child-added', function(child){
                self.visit(child);
                // Show/Hide scrolls.
                self._showHideScrolls(self.getChildContainer().node().ownerSVGElement.parentElement, self.getChildContainer().node().ownerSVGElement);
            });

            this._variableButton = VariablesView.createVariableButton(this.getChildContainer().node(),
                parseInt(this.getChildContainer().attr("x")) + 4, parseInt(this.getChildContainer().attr("y")) + 7);

            var variableProperties = {
                model: this._model,
                activatorElement: this._variableButton,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        x: parseInt(this.getChildContainer().attr("x")) + 17,
                        y: parseInt(this.getChildContainer().attr("y")) + 6
                    },
                    width: parseInt(this.getChildContainer().node().getBBox().width) - (2 * $(this._variableButton).width())
                }
            };

            this._variablePane = VariablesView.createVariablePane(variableProperties);

            var annotationProperties = {
                model: this._model,
                activatorElement: headingAnnotationIcon.node(),
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        // "-1" to remove the svg stroke line
                        left: parseFloat(this.getChildContainer().attr("x")) + parseFloat(this.getChildContainer().attr("width")) - 1,
                        top: this.getChildContainer().attr("y")
                    }
                }
            };

            AnnotationView.createAnnotationPane(annotationProperties);

            var argumentsProperties = {
                model: this._model,
                activatorElement: headingArgumentsIcon.node(),
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        // "-1" to remove the svg stroke line
                        left: parseFloat(this.getChildContainer().attr("x")) + parseFloat(this.getChildContainer().attr("width")) - 1,
                        top: this.getChildContainer().attr("y")
                    }
                }
            };

            ArgumentsView.createArgumentsPane(argumentsProperties);

            var returnTypeProperties = {
                model: this._model,
                activatorElement: headingReturnTypesIcon.node(),
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        left: parseFloat(this.getChildContainer().attr("x")) + parseFloat(this.getChildContainer().attr("width")) - 1,
                        top: this.getChildContainer().attr("y")
                    }
                }
            };

            // Creating return type pane.
            ReturnTypeView.createReturnTypePane(returnTypeProperties);

            var operationButtons = [headingAnnotationIcon.node(), headingArgumentsIcon.node()];

            // Closing the shown pane when another operation button is clicked.
            _.forEach(operationButtons, function (button) {
                $(button).click(function(event){
                    event.stopPropagation();
                });

                $(button).click(function () {
                    _.forEach(operationButtons, function (buttonToClick) {
                        if (button !== buttonToClick && $(buttonToClick).data("showing-pane") == "true") {
                            $(buttonToClick).click();
                        }
                    });
                });
            });

            this.getBoundingBox().on("moved", function(offset){
                var currentTransform = this._connectorActionGroup.attr("transform");
                this._connectorActionGroup.attr("transform", (!_.isNil(currentTransform) ? currentTransform : "") +
                    " translate(" + offset.dx + ", " + offset.dy + ")");

                // Reposition the connector action name container
                var newDivPositionVertical = parseInt(nameDiv.css("top")) + offset.dy;
                nameDiv.css("top", newDivPositionVertical + "px");

                // Reposition Variable button
                var newVButtonPositionVertical = parseInt($(self._variableButton).css("top")) + offset.dy;
                $(self._variableButton).css("top", newVButtonPositionVertical + "px");

                // Reposition variable pane
                var newVPanePositionVertical = parseInt($(self._variablePane).css("top")) + offset.dy;
                $(self._variablePane).css("top", newVPanePositionVertical + "px");
            }, this);
        };

        /**
         * Shows and hide the custom scrolls depending on the amount scrolled.
         * @param {Element} container - The container of the SVG. i.e the parent of the SVG.
         * @param {Element} svgElement - The SVG element.
         */
        ConnectorActionView.prototype._showHideScrolls = function (container, svgElement) {
            // Creating scroll panes.
            var leftScroll = $(container).find(".service-left-scroll").get(0);
            var rightScroll = $(container).find(".service-right-scroll").get(0);

            // Setting heights of the scrolls.
            $(leftScroll).height($(container).height());
            $(rightScroll).height($(container).height());

            // Positioning the arrows of the scrolls.
            $(leftScroll).find("i").css("padding-top", ($(container).height() / 2) - (parseInt($(leftScroll).find("i").css("font-size"), 10) / 2) + "px");
            $(rightScroll).find("i").css("padding-top", ($(container).height() / 2) - (parseInt($(rightScroll).find("i").css("font-size"), 10) / 2) + "px");

            // Showing/Hiding scrolls.
            if (Math.abs($(container).width() - $(svgElement).width()) < 5) {
                // If the svg width is less than or equal to the container, then no need to show the arrows.
                $(leftScroll).hide();
                $(rightScroll).hide();
            } else {
                // If the svg width is greater than the width of the container...
                if ($(container).scrollLeft() == 0) {
                    // When scrollLeft is 0, means that it is already scrolled to the left corner.
                    $(rightScroll).show();
                    $(leftScroll).hide();
                } else if ($(container).scrollLeft() == parseInt($(svgElement).width(), 10) -
                    parseInt($(container).width(), 10)) {
                    // When scrolled all the way to the right.
                    $(leftScroll).show();
                    $(rightScroll).hide();
                } else {
                    // When scrolled to the middle.
                    $(leftScroll).show();
                    $(rightScroll).show();
                }
            }
        };

        /**
         * Render statement container
         */
        ConnectorActionView.prototype.renderStatementContainer = function(){
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', this._model);
            _.set(statementContainerOpts, 'topCenter', this._defaultWorker.getTopCenter());
            _.set(statementContainerOpts, 'bottomCenter', this._defaultWorker.getBottomCenter());
            _.set(statementContainerOpts, 'width', this._defaultWorker.width());
            _.set(statementContainerOpts, 'container', this._defaultWorker.getContentArea().node());
            _.set(statementContainerOpts, 'toolPalette', this.toolPalette);
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this.listenTo(this._statementContainer.getBoundingBox(), 'bottom-edge-moved', function(dy){
                this._defaultWorker.getBottomCenter().y(this._statementContainer.getBoundingBox().getBottom());
                this.getBoundingBox().h(this.getBoundingBox().h() + dy);
            });
            this._statementContainer.render(this.diagramRenderingContext);
        };

        ConnectorActionView.prototype.initActionLevelDropTarget = function(){
            var self = this,
                hoverClass = this._viewOptions.hoverClass;
            var mouseOverHandler = function() {
                //if someone is dragging a tool from tool-palette
                if(self.toolPalette.dragDropManager.isOnDrag()){

                    if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)){
                        return;
                    }

                    // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                    // tool view will use this to provide feedback on impossible drop zones
                    self.toolPalette.dragDropManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                        var nodeFactory = self._model.getFactory();
                        // IMPORTANT: override connector action node's default validation logic
                        // This drop zone is for worker and connector declarations only.
                        // Statements should only be allowed on top of default worker's drop zone.
                        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
                            || nodeFactory.isWorkerDeclaration(nodeBeingDragged);
                    });

                    // indicate drop area
                    self._contentRect.classed(hoverClass, true);
                    self._headingRect.classed(hoverClass, true);

                    // reset ui feed back on drop target change
                    self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                        self._contentRect.classed(hoverClass, false);
                        self._headingRect.classed(hoverClass, false);
                    });
                }
                d3.event.stopPropagation();
                //TODO : Remove this and set currentScope/connector action properly
                self.diagramRenderingContext.currentConnectorAction = self;

            };

            var mouseOutHandler = function() {
                // reset ui feed back on hover out
                if(self.toolPalette.dragDropManager.isOnDrag()){
                    if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                        self._contentRect.classed('design-view-hover-svg', false);
                        self._headingRect.classed('design-view-hover-svg', false);
                    }
                }
                d3.event.stopPropagation();

            };
            this._contentRect.on("mouseover", mouseOverHandler);
            this._headingRect.on("mouseover", mouseOverHandler);
            this._contentRect.on("mouseout", mouseOutHandler);
            this._headingRect.on("mouseout", mouseOutHandler);
        };

        ConnectorActionView.prototype.getConnectorWorkerViewList = function(){
            return this._connectorWorkerViewList;
        };
        /**
         * @inheritDoc
         * @returns {_defaultWorker}
         */
        ConnectorActionView.prototype.getDefaultWorker = function () {
            return this._defaultWorker;
        };

        ConnectorActionView.prototype.canVisitConnectorAction = function (connectorAction) {
            return true;
        };

        ConnectorActionView.prototype.canVisitConnectorDeclaration = function (connectorDeclaration) {
            return true;
        };

        /**
         * Calls the render method for a connector declaration.
         * @param {ConnectorDeclaration} connectorDeclaration - The connector declaration model.
         */
        ConnectorActionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
            var self = this;
            var connectorContainer = this._contentGroup.node();
            var connectorOpts = {
                    model: connectorDeclaration,
                    container: connectorContainer,
                    parentView: this,
                    messageManager: this.messageManager,
                    lineHeight: this._defaultWorker.getTopCenter().absDistInYFrom(this._defaultWorker.getBottomCenter())
            };
            var connectorDeclarationView, center;

            var lastLifeLine = this.getLastLifeLine();
            center = lastLifeLine.getTopCenter().clone().move(this._viewOptions.LifeLineCenterGap, 0);

            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            this._connectorWorkerViewList.push(connectorDeclarationView);

            connectorDeclarationView._rootGroup.attr('id', '_' +connectorDeclarationView._model.id);

            connectorDeclarationView.render();

            // If the New Connector or the worker goes out of the action bounding box we expand the action BBox
            if (connectorDeclarationView.getBoundingBox().getRight() > this.getBoundingBox().getRight()) {
                this._parentView.getLifeLineMargin().setPosition(this._parentView.getLifeLineMargin().getPosition() + this._viewOptions.LifeLineCenterGap);
                this.setContentMinWidth(connectorDeclarationView.getBoundingBox().getRight());
                this.setHeadingMinWidth(connectorDeclarationView.getBoundingBox().getRight());
            }

            // Creating property pane
            var editableProperties = [
                {
                    propertyType: "text",
                    key: "Name",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getConnectorVariable,
                    setterMethod: connectorDeclarationView._model.setConnectorVariable
                },
                {
                    propertyType: "text",
                    key: "Uri",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getUri,
                    setterMethod: connectorDeclarationView._model.setUri
                }
            ];
            connectorDeclarationView.createPropertyPane({
                model: connectorDeclarationView._model,
                lifeLineGroup:connectorDeclarationView._rootGroup,
                editableProperties: editableProperties
            });

            connectorDeclarationView.setParent(this);

            this.getBoundingBox().on("height-changed", function (dh) {
                this.getBoundingBox().h(this.getBoundingBox().h() + dh);
            }, connectorDeclarationView);

            this.trigger("childConnectorViewAddedEvent", connectorDeclarationView);
        };

        /**
         * setting connector action container height and setting the height for the bounding box
         * @param height
         */
        ConnectorActionView.prototype.setConnectorActionContainerHeight = function (height){
            this._connectorActionGroup.attr("height", height);
            this._contentRect.attr("height", height);
            this._defaultWorker.setHeight(height - this._viewOptions.totalHeightGap);
            this.getBoundingBox().h(height);
        };

        ConnectorActionView.prototype.getLastLifeLine = function () {
            if(this.getConnectorWorkerViewList().length > 0 ){
                return _.last(this.getConnectorWorkerViewList());
            }
            else{
                return this.getDefaultWorker();
            }
        };

        /**
         * get the Statement View List of the the connector action
         * @returns [_statementExpressionViewList] {Array}
         */
        ConnectorActionView.prototype.getStatementExpressionViewList = function () {
            return this._statementExpressionViewList;
        };

        /**
         * Y distance from one connector action's end point to next connector action's start point
         * @returns {number}
         */
        ConnectorActionView.prototype.getGapBetweenConnectorActions = function () {
            return 25;
        };

        /**
         * Y distance from one statement's end point to next statement's start point
         * @returns {number}
         */
        ConnectorActionView.prototype.getGapBetweenStatements = function () {
            return 10;
        };

        /**
         * Height of the action's heading
         * @returns {number}
         */
        ConnectorActionView.prototype.getConnectorActionHeadingHeight = function () {
            return this._viewOptions.heading.height;
        };

        /**
         * Minimum width of the content area
         * @returns {number} Minimum content width
         */
        ConnectorActionView.prototype.getContentMinWidth = function () {
            return this._viewOptions.contentMinWidth;
        };

        /**
         * Set Minimum width of the content area
         * @param {number} minWidth - Minimum width
         */
        ConnectorActionView.prototype.setContentMinWidth = function (minWidth) {
            this._viewOptions.contentMinWidth = minWidth;
        };

        /**
         * Set Minimum width of the heading
         * @param {number} minWidth - Minimum width
         */
        ConnectorActionView.prototype.setHeadingMinWidth = function (minWidth) {
            this._viewOptions.heading.minWidth = minWidth;
        };

        /**
         * Minimum width of the heading
         * @returns {number} Minimum Heading Width
         */
        ConnectorActionView.prototype.getHeadingMinWidth = function () {
            return this._viewOptions.heading.minWidth;
        };

        /**
         * Shrink or Expand the Connector action view
         * @param {number} dw - delta width
         * @returns {boolean} - Shrink or expanded
         */
        ConnectorActionView.prototype.ShrinkOrExpand = function (dw) {
            if (this.getBoundingBox().w() + dw > this._viewOptions.contentMinWidth) {
                this.getBoundingBox().w(this.getBoundingBox().w() + dw);
                return true;
            } else {
                return false;
            }
        };

        return ConnectorActionView;
    });
