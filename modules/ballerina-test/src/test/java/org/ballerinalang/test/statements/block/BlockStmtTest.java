/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.block;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlockStmtTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/statements/block/block-stmt.bal");
        resultNegative = BTestUtils.compile("test-src/statements/block/block-stmt-negative.bal");
    }

    @Test
    public void blockStmtTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test(description = "Test block statement with errors")
    public void testBlockStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 10);
        //testUnreachableStmtInIfFunction1
        BTestUtils.validateError(resultNegative, 0, "unreachable code", 9, 5);
        //testUnreachableStmtInIfFunction2
        BTestUtils.validateError(resultNegative, 1, "unreachable code", 25, 5);
        //testUnreachableStmtInIfBlock
        BTestUtils.validateError(resultNegative, 2, "unreachable code", 33, 9);
        //testUnreachableStmtInWhileBlock
        BTestUtils.validateError(resultNegative, 3, "unreachable code", 46, 13);
        //testCommentAfterReturnStmt
        BTestUtils.validateError(resultNegative, 4, "unreachable code", 62, 5);
        //testUnreachableTryCatch
        BTestUtils.validateError(resultNegative, 5, "unreachable code", 73, 5);
        //testUnreachableNext
        BTestUtils.validateError(resultNegative, 6, "unreachable code", 84, 9);
        //testUnreachableBreak
        BTestUtils.validateError(resultNegative, 7, "unreachable code", 92, 9);
        BTestUtils.validateError(resultNegative, 8, "break cannot be used outside of a loop", 92, 9);
        //testUnreachableThrow
        BTestUtils.validateError(resultNegative, 9, "unreachable code", 108, 9);

    }
}
