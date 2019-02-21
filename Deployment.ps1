# Check for existing folder structure
if (Test-Path .\build) {
	echo "The build folder already exists! Do you want to overwrite?"
	$Choice = Read-Host -Prompt "(y/n)"
	if ($Choice -ne "y" ) {
		break
	}
}

# Remove any existing folder structure
Remove-Item -R ".\build"
echo "Folder structure cleaned up"

# Create folder structure
New-Item -Path "." -Name "build" -ItemType "directory" > $null
New-Item -Path ".\build" -Name "modules" -ItemType "directory" > $null
echo "New folder structure created"

# Create application JAR from class files
jar -c -f ".\build\VocabularyTrial.jar" -C ".\bin" "."
echo "Application JAR generated"

# Create application java module files
jmod create --class-path ".\build\VocabularyTrial.jar" --main-class "com.visparu.vocabularytrial.root.Main" ".\visparu.vocabularytrial.jmod"
echo "Application module generated"

# Create dependency legacy java module files
jmod create --class-path ".\lib\sqlite-jdbc-*.jar" ".\sqlite.jdbc.jmod"
jmod create --class-path ".\lib\json-simple-*.jar" ".\json.simple.jmod"
echo "Dependency modules generated"

# Move generated files to modules folder
mv ".\visparu.vocabularytrial.jmod" ".\build\modules"
mv ".\sqlite.jdbc.jmod" ".\build\modules"
mv ".\json.simple.jmod" ".\build\modules"
cp ".\lib\javafx-jmods-11*\*" ".\build\modules"
echo "Files transferred to final location"

# Link modules together to create finished build
jlink --output ".\build\image" `
--module-path ".\build\modules" `
--add-modules "visparu.vocabularytrial" `
--launcher "VocabularyTrial=visparu.vocabularytrial"
echo "Application linked and provisioned to the build folder"

# Replace execution environment "java" with "javaw"
((Get-Content -Path ".\build\image\bin\VocabularyTrial.bat" -Raw) -replace '"%DIR%\\java"','start javaw') | `
Set-Content -Path ".\build\image\bin\VocabularyTrial.bat"
echo "Corrected execution environment"