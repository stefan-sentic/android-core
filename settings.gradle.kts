/*
 * Copyright 2023 HTEC Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
	// See https://jmfayard.github.io/refreshVersions
	id("de.fayard.refreshVersions") version "0.51.0"
}

refreshVersions {
	enableBuildSrcLibs()
	rejectVersionIf {
		candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
	}
}

include(":domain", ":data", ":presentation", ":presentation-databinding", ":test", ":bom")
System.setProperty("rootDir", rootDir.toString())