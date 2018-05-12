package com.aemtools.integration.sync.action

import com.aemtools.integration.sync.logger.CrxStatusLogger
import com.aemtools.integration.sync.packmgr.uninstall.PackageUninstaller
import com.aemtools.integration.sync.settings.AemToolsProjectConfiguration
import com.aemtools.integration.sync.util.SyncConstants
import com.aemtools.integration.sync.util.getJcrPath
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder
import io.wcm.tooling.commons.packmgr.PackageManagerProperties
import io.wcm.tooling.commons.packmgr.install.PackageFile
import io.wcm.tooling.commons.packmgr.install.PackageInstaller
import io.wcm.tooling.commons.packmgr.install.VendorInstallerFactory
import java.io.File

/**
 * @author Dmytro Liakhov
 */
class ExportFileToInstance : AbstractSyncAction() {

  override fun actionPerformed(event: AnActionEvent) {
    val zipFile = File(SyncConstants.TMP_FILE_PACKAGE_NAME)
    val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        ?.firstOrNull() ?: return

    val project = event.project ?: return

    val pathOnAEMInstance = virtualFile.getJcrPath()
    val rootPath = pathOnAEMInstance.substringBeforeLast("/")
    val projectConfiguration = AemToolsProjectConfiguration.getInstance(project)

    val builder = ContentPackageBuilder()
        .name(SyncConstants.TMP_NAME)
        .group(SyncConstants.TMP_GROUP)
        .rootPath(rootPath)

    builder.build(zipFile).use { contentPackage ->
      val myFile = File(virtualFile.path)
      contentPackage.addFile(pathOnAEMInstance, myFile)
    }

    val props = PackageManagerProperties()

    val instance = projectConfiguration.instances[0]
    props.userId = instance.credentials.login
    props.password = instance.credentials.password
    props.packageManagerUrl = "${instance.address}/${VendorInstallerFactory.CRX_URL}"

    val packageFile = PackageFile()
    packageFile.file = zipFile
    packageFile.setForce(true)

    val logger = CrxStatusLogger()
    val packageInstaller = PackageInstaller(props, logger)
    packageInstaller.installFile(packageFile)

    val uninstallerPackage = PackageUninstaller(props, logger)
    uninstallerPackage.uninstallFile(SyncConstants.TMP_NAME)
  }

}
