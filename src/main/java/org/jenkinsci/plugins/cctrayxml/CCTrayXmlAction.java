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
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
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
            return view.getOwner().getItemGroup().getAllItems(TopLevelItem.class);
        } else {
            List<TopLevelItem> items = new ArrayList<>();

            items.addAll(view.getItems()); // get all top-level items in the default view

            // and find all multi-branch default items (HE addition)
            items.addAll(view.getOwner().getItemGroup().getAllItems(WorkflowJob.class,
                    (WorkflowJob p) -> p.getAction(PrimaryInstanceMetadataAction.class) != null)
            );

            return items;
        }
    }
}
