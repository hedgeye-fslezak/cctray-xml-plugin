/*
 * The MIT License
 *
 * Copyright (c) 2016 Daniel Beck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.cctrayxml;

import hudson.model.*;
import jenkins.model.Jenkins;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Stapler;

import java.util.*;

@Restricted(NoExternalUse.class)
public class CCTrayXmlAction implements Action {

    private transient View view;

    CCTrayXmlAction(View view) {
        this.view = view;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "cc.xml";
    }

    public View getView() {
        return this.view;
    }

    /**
     * Converts the Hudson build status to CruiseControl build status,
     * which is either Success, Failure, Exception, or Unknown.
     */
    public String toCCStatus(Item i) {
        if (i instanceof Job) {
            Job j = (Job) i;
            switch (j.getIconColor()) {
                case ABORTED:
                case ABORTED_ANIME:
                case RED:
                case RED_ANIME:
                case YELLOW:
                case YELLOW_ANIME:
                    return "Failure";
                case BLUE:
                case BLUE_ANIME:
                    return "Success";
                case DISABLED:
                case DISABLED_ANIME:
                case GREY:
                case GREY_ANIME:
                case NOTBUILT:
                case NOTBUILT_ANIME:
                    return "Unknown";
            }
        }
        return "Unknown";
    }

    @Restricted(NoExternalUse.class) // Jelly
    public Collection<TopLevelItem> getCCItems() {
        if (Stapler.getCurrentRequest().getParameter("recursive") != null) {
            Collection<TopLevelItem> allItems = view.getOwner().getItemGroup().getAllItems(TopLevelItem.class);

            if (Stapler.getCurrentRequest().getParameter("everything") != null) {
                return allItems;
            }

            // filter out jobs that aren't the default branch of a multibranch pipeline
            allItems.removeIf((TopLevelItem i) -> i instanceof WorkflowJob
                    && ((WorkflowJob) i).getParent() instanceof WorkflowMultiBranchProject
                    && ((WorkflowJob) i).getAction(PrimaryInstanceMetadataAction.class) == null);

            return allItems;
        } else {
            var viewBaseItems = view.getItems();

            List<TopLevelItem> items = new ArrayList<>(viewBaseItems);

            // lightly recurse to capture extras
            for (TopLevelItem item : viewBaseItems) {
                if (!(item instanceof WorkflowMultiBranchProject)) continue;

                // Capture default branches from multibranch jobs
                items.addAll(((WorkflowMultiBranchProject) item).getAllItems(WorkflowJob.class,
                        (WorkflowJob j) -> j.getAction(PrimaryInstanceMetadataAction.class) != null
                ));
            }

            return items;
        }
    }
}
