/*
 * Copyright (c) 2024. Bernard Bou <1313ou@gmail.com>
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

// C O R E

include(":treebolicGlue")
project(":treebolicGlue").projectDir = File("../TreebolicLib/treebolicGlue/")

include(":treebolicIface")
project(":treebolicIface").projectDir = File("../TreebolicLib/treebolicIface/")

// B A S E A P P

include(":treebolic")
project(":treebolic").projectDir = File("../Treebolic/treebolic/")

// L I B S

include(":commonLib")
project(":commonLib").projectDir = File("../TreebolicSupportLibs/commonLib/")

include(":searchLib")
project(":searchLib").projectDir = File("../TreebolicSupportLibs/searchLib/")

include(":wheelLib")
project(":wheelLib").projectDir = File("../TreebolicSupportLibs/wheelLib/")

include(":guideLib")
project(":guideLib").projectDir = File("../TreebolicSupportLibs/guideLib/")

include(":downloadLib")
project(":downloadLib").projectDir = File("../TreebolicSupportLibs/downloadLib/")

include(":preferenceLib")
project(":preferenceLib").projectDir = File("../TreebolicSupportLibs/preferenceLib/")

include(":fileChooserLib")
project(":fileChooserLib").projectDir = File("../TreebolicSupportLibs/fileChooserLib/")

include(":storageLib")
project(":storageLib").projectDir = File("../TreebolicSupportLibs/storageLib/")

include(":rateLib")
project(":rateLib").projectDir = File("../TreebolicSponsorLibs/rateLib/")

include(":othersLib")
project(":othersLib").projectDir = File("../TreebolicSponsorLibs/othersLib/")

include(":donateLib")
project(":donateLib").projectDir = File("../TreebolicSponsorLibs/donateLib/")

// P R O V I D E R S

include(":treebolicFilesProvider")
project(":treebolicFilesProvider").projectDir = File("../TreebolicFiles/treebolicFilesProvider/")

include(":treebolicOwlProvider")
project(":treebolicOwlProvider").projectDir = File("../TreebolicOne/treebolicOwlProvider/")

include(":treebolicWordNetProvider")
project(":treebolicWordNetProvider").projectDir = File("../TreebolicWordNet/treebolicWordNetProvider/")

// LIB

include(":treebolicAidl")
include(":treebolicClientsLib")
include(":treebolicServicesLib")
include(":treebolicServicesIface")
include(":treebolicClientsIface")

// PARCEL

include(":treebolicParcel")

// APPS

include(":treebolicFilesServices")
include(":treebolicOwlServices")
include(":treebolicWordNetServices")
