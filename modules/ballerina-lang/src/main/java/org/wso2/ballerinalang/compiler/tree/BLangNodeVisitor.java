/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn.BLangWorkerReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransform;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * @since 0.94
 */
public abstract class BLangNodeVisitor {

    public void visit(BLangPackage pkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangCompilationUnit compUnit) {
        throw new AssertionError();
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        throw new AssertionError();
    }

    public void visit(BLangImportPackage importPkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        throw new AssertionError();
    }

    public void visit(BLangService serviceNode) {
        throw new AssertionError();
    }

    public void visit(BLangResource resourceNode) {
        throw new AssertionError();
    }

    public void visit(BLangConnector connectorNode) {
        throw new AssertionError();
    }

    public void visit(BLangAction actionNode) {
        throw new AssertionError();
    }

    public void visit(BLangStruct structNode) {
        throw new AssertionError();
    }

    public void visit(BLangEnum enumNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariable varNode) {
        throw new AssertionError();
    }

    public void visit(BLangWorker workerNode) {
        throw new AssertionError();
    }

    public void visit(BLangIdentifier identifierNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotation annotationNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        throw new AssertionError();
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariableDef varDefNode) {
        throw new AssertionError();
    }

    public void visit(BLangAssignment assignNode) {
        throw new AssertionError();
    }

    public void visit(BLangAbort abortNode) {
        throw new AssertionError();
    }

    public void visit(BLangNext continueNode) {
        throw new AssertionError();
    }

    public void visit(BLangBreak breakNode) {
        throw new AssertionError();
    }

    public void visit(BLangReturn returnNode) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerReturn returnNode) {
        throw new AssertionError();
    }

    public void visit(BLangRetry retryNode) {
        throw new AssertionError();
    }

    public void visit(BLangThrow throwNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangComment commentNode) {
        throw new AssertionError();
    }

    public void visit(BLangIf ifNode) {
        throw new AssertionError();
    }

    public void visit(BLangWhile whileNode) {
        throw new AssertionError();
    }

    public void visit(BLangTransaction transactionNode) {
        throw new AssertionError();
    }

    public void visit(BLangTransform transformNode) {
        throw new AssertionError();
    }

    public void visit(BLangTryCatchFinally tryNode) {
        throw new AssertionError();
    }

    public void visit(BLangCatch catchNode) {
        throw new AssertionError();
    }

    public void visit(BLangForkJoin forkJoin) {
        throw new AssertionError();
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        throw new AssertionError();
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        throw new AssertionError();
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangInvocation invocationExpr) {
        throw new AssertionError();
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        throw new AssertionError();
    }

    public void visit(BLangActionInvocation actionInvocationExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeCastExpr castExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        throw new AssertionError();
    }

    public void visit(BLangXMLQName xmlQName) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        throw new AssertionError();
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerSend workerSendNode) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        throw new AssertionError();
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        throw new AssertionError();
    }

    // Type nodes

    public void visit(BLangValueType valueType) {
        throw new AssertionError();
    }

    public void visit(BLangArrayType arrayType) {
        throw new AssertionError();
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        throw new AssertionError();
    }

    public void visit(BLangConstrainedType constrainedType) {
        throw new AssertionError();
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        throw new AssertionError();
    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {
        throw new AssertionError();
    }


    // expressions that will used only from the Desugar phase

    public void visit(BLangLocalVarRef localVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangFieldVarRef fieldVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangPackageVarRef packageVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangFunctionVarRef functionVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangJSONLiteral jsonLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangMapLiteral mapLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangStructLiteral structLiteral) {
        throw new AssertionError();
    }

    public void visit(BFunctionPointerInvocation bFunctionPointerInvocation) {
        throw new AssertionError();
    }

    public void visit(BLangInvocation.BLangFunctionInvocation iExpr) {
        throw new AssertionError();
    }

    public void visit(BLangJSONArrayLiteral jsonArrayLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
        throw new AssertionError();
    }
    
    public void visit(BLangLocalXMLNS xmlnsNode) {
        throw new AssertionError();
    }
    
    public void visit(BLangPackageXMLNS xmlnsNode) {
        throw new AssertionError();
    }
}
