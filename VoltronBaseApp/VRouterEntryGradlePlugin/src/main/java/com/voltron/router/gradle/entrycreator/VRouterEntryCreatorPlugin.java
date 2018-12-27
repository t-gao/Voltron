package com.voltron.router.gradle.entrycreator;

import com.android.build.gradle.BaseExtension;
import com.voltron.router.gradle.utils.Logger;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VRouterEntryCreatorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Logger.init(project);
        Logger.i("VRouterEntryCreatorPlugin apply()");
        Logger.i("project: " + project.getDisplayName());

        project.getExtensions().findByType(BaseExtension.class)
                .registerTransform(new VRouterEntryCreatorTransform());
    }
}
