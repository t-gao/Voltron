package com.voltron.router.gradle;

import com.android.build.gradle.BaseExtension;
import com.voltron.router.gradle.autowired.AutowiredTransform;
import com.voltron.router.gradle.entrycreator.VRouterEntryCreatorTransform;
import com.voltron.router.gradle.utils.Logger;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VRouterEntryCreatorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Logger.init(project);
        Logger.i("VRouterEntryCreatorPlugin apply()");
        Logger.i("project: " + project.getDisplayName());

        BaseExtension extension = project.getExtensions().findByType(BaseExtension.class);
        if (extension != null) {
            extension.registerTransform(new VRouterEntryCreatorTransform());
            extension.registerTransform(new AutowiredTransform());
        }
    }
}
