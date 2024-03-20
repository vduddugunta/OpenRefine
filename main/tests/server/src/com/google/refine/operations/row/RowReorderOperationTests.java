/*******************************************************************************
 * Copyright (C) 2018, OpenRefine contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.google.refine.operations.row;

import java.io.Serializable;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.google.refine.ProjectManager;
import com.google.refine.RefineTest;
import com.google.refine.browsing.Engine.Mode;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Cell;
import com.google.refine.model.Project;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.sorting.SortingConfig;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.TestUtils;

public class RowReorderOperationTests extends RefineTest {

    Project project = null;

    @BeforeSuite
    public void registerOperation() {
        OperationRegistry.registerOperation(getCoreModule(), "row-reorder", RowReorderOperation.class);
    }

    @BeforeMethod
    public void setUp() {
        project = createProject(
                new String[] { "key", "first" },
                new Serializable[][] {
                        { "8", "b" },
                        { null, "d" },
                        { "2", "f" },
                        { "1", "h" },
                        { "9", "F" },
                        { "10", "f" }
                });
    }

    @AfterMethod
    public void tearDown() {
        ProjectManager.singleton.deleteProject(project.id);
    }

    @Test
    public void testSortEmptyString() throws Exception {
        String sortingJson = "{\"criteria\":[{\"column\":\"key\",\"valueType\":\"number\",\"reverse\":false,\"blankPosition\":2,\"errorPosition\":1}]}";
        SortingConfig sortingConfig = SortingConfig.reconstruct(sortingJson);
        project.rows.get(1).cells.set(0, new Cell("", null));
        AbstractOperation op = new RowReorderOperation(
                Mode.RowBased, sortingConfig);

        runOperation(op, project);

        Project expectedProject = createProject(
                new String[] { "key", "first" },
                new Serializable[][] {
                        { "1", "h" },
                        { "2", "f" },
                        { "8", "b" },
                        { "9", "F" },
                        { "10", "f" },
                        { "", "d" },
                });
        assertProjectEquals(project, expectedProject);
    }

    @Test
    public void testReverseSort() throws Exception {
        String sortingJson = "{\"criteria\":[{\"column\":\"key\",\"valueType\":\"number\",\"reverse\":true,\"blankPosition\":-1,\"errorPosition\":1}]}";
        SortingConfig sortingConfig = SortingConfig.reconstruct(sortingJson);
        project.rows.get(1).cells.set(0, new Cell("", null));
        AbstractOperation op = new RowReorderOperation(
                Mode.RowBased, sortingConfig);

        runOperation(op, project);

        Project expectedProject = createProject(
                new String[] { "key", "first" },
                new Serializable[][] {
                        { "", "d" },
                        { "10", "f" },
                        { "9", "F" },
                        { "8", "b" },
                        { "2", "f" },
                        { "1", "h" },
                });
        assertProjectEquals(project, expectedProject);
    }

    @Test
    public void testStringSort() throws Exception {
        String sortingJson = "{\"criteria\":[{\"column\":\"first\",\"valueType\":\"string\",\"reverse\":false,\"blankPosition\":2,\"errorPosition\":1,\"caseSensitive\":true}]}";
        SortingConfig sortingConfig = SortingConfig.reconstruct(sortingJson);
        project.rows.get(1).cells.set(0, new Cell("", null));
        AbstractOperation op = new RowReorderOperation(
                Mode.RowBased, sortingConfig);

        runOperation(op, project);

        Project expected = createProject(
                new String[] { "key", "first" },
                new Serializable[][] {
                        { "8", "b" },
                        { "", "d" },
                        { "2", "f" },
                        { "10", "f" },
                        { "9", "F" },
                        { "1", "h" },
                });
        assertProjectEquals(project, expected);
    }

    @Test
    public void serializeRowReorderOperation() throws Exception {
        String json = "  {\n" +
                "    \"op\": \"core/row-reorder\",\n" +
                "    \"description\": \"Reorder rows\",\n" +
                "    \"mode\": \"record-based\",\n" +
                "    \"sorting\": {\n" +
                "      \"criteria\": [\n" +
                "        {\n" +
                "          \"errorPosition\": 1,\n" +
                "          \"valueType\": \"number\",\n" +
                "          \"column\": \"start_year\",\n" +
                "          \"blankPosition\": 2,\n" +
                "          \"reverse\": false\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }";
        TestUtils.isSerializedTo(ParsingUtilities.mapper.readValue(json, RowReorderOperation.class), json);
    }

}
