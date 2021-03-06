/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.java.bridges;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.java.JavaSquid;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourcePackage;
import org.sonar.squid.api.SourceProject;
import org.sonar.squid.indexer.QueryByType;
import org.sonar.squid.indexer.SquidIndex;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public final class ResourceIndex extends HashMap<SourceCode, Resource> {

  private static final long serialVersionUID = -918346378374943773L;

  public ResourceIndex loadSquidResources(JavaSquid squid, SensorContext context, Project project) {
    loadSquidProject(squid.getIndex(), project);
    loadSquidFilesAndPackages(squid.getIndex(), context, project);
    return this;
  }

  private void loadSquidProject(SquidIndex squid, Project project) {
    put(squid.search(new QueryByType(SourceProject.class)).iterator().next(), project);
  }

  /**
   * @see org.sonar.java.ast.visitors.FileVisitor
   * @see org.sonar.java.ast.visitors.PackageVisitor
   */
  private void loadSquidFilesAndPackages(SquidIndex squid, SensorContext context, Project project) {
    Collection<SourceCode> files = squid.search(new QueryByType(SourceFile.class));
    for (SourceCode squidFile : files) {
      String filePath = squidFile.getName();

      Resource sonarFile = org.sonar.api.resources.File.fromIOFile(new File(filePath), project);
      // resource is reloaded to get the id:
      put(squidFile, context.getResource(sonarFile));

      SourceCode squidPackage = squidFile.getParent(SourcePackage.class);
      Resource sonarDirectory = sonarFile.getParent();
      put(squidPackage, context.getResource(sonarDirectory));
    }
  }

}
