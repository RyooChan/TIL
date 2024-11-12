# IntelliJ plugin을 만들고 배포해 보자 - method tester (2)

**(이 내용은 플러그인의 구성 요소와 내용에 관한 글이다. 어떤 로직을 어떻게 구현했는지와 대략적인 플러그인 플로우에 관한 것.)**

[이전 글](https://hello-backend.tistory.com/333)에서 어떤 것을 만들지에 대한 내용을 찾았다.
그러면 이를 실제로 한번 만들어 볼 것이다.

https://github.com/JetBrains/intellij-platform-plugin-template

젯브레인에서는 플러그인을 만들기 위한 템플렛을 지원한다.
플러그인을 만들기 위해서 여기서 fork해서 템플렛을 복제한다.

그리고 하나씩 원하는 기능을 만들어 보면 된다.

![image](https://github.com/user-attachments/assets/cee28b76-4c2a-48aa-a154-716c4ce4da50)

- plugin.xml
    - 플러그인의 정보와 이후 기능을 부여할 action 동작에 관한 기본적인 정의를 하는 부분이라고 생각하면 된다.

```
<!-- Plugin Configuration File. Read more: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html -->
<idea-plugin>
    <!-- Unique identifier of the plugin. It should be FQN. It cannot be changed between the plugin versions. -->
    <id>com.github.ryoochan.acmt</id>

    <!-- Public plugin name should be written in Title Case.
         Guidelines: https://plugins.jetbrains.com/docs/marketplace/plugin-overview-page.html#plugin-name -->
    <name>All-Call Method Tester</name>

    <!-- A displayed Vendor name or Organization ID displayed on the Plugins Page. -->
    <vendor email="fbcks97@naver.com" url="fbcks97@naver.com">RyooChan</vendor>

    <!-- Description of the plugin displayed on the Plugin Page and IDE Plugin Manager.
         Simple HTML elements (text formatting, paragraphs, and lists) can be added inside of <![CDATA[ ]]> tag.
         Guidelines: https://plugins.jetbrains.com/docs/marketplace/plugin-overview-page.html#plugin-description -->
    <description><![CDATA[
        You can test all the test cases where a method is used at once.
        Right-click on the desired method and select "Run Related Tests."
  ]]></description>

    <!-- Product and plugin compatibility requirements.
         Read more: https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html -->
    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>
    <depends>JUnit</depends>

    <!-- Extension points defined by the plugin.
         Read more: https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html -->
    <extensions defaultExtensionNs="com.intellij">

    </extensions>

    <actions>
        <!-- Add your actions here -->
        <action id="RunRelatedTestsAction"
                class="spring.methodtester.RunRelatedTestsAction"
                text="Run Related Tests"
                description="Runs tests related to the current context">
            <add-to-group group-id="EditorPopupMenu" anchor="last"/>
        </action>
    </actions>
</idea-plugin>
```

나는 이런 식으로 적어 두었다.
대략적인 동작 방식이나 이름, 사용하는 모듈 등에 대한 내용을 적어 두었고, 저기서 특히 action에 있는 부분이 실제로 어떻게 할지에 대한 것이라 보면 된다.
나는 마우스 오른쪽 버튼을 눌렀을 때에 나타나도록 했다. (https://intellij-support.jetbrains.com/hc/en-us/community/posts/4404273616018-Add-an-action-to-EditorPopupMenu-when-right-clicking-on-a-test-method-name)

- RunRelatedTestsAction.kt

```
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class RunRelatedTestsAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        val psiFile: PsiFile? = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE)

        psiFile?.let { file ->
            editor?.let { editor ->
                val offset = editor.caretModel.offset
                val elementAtCaret: PsiElement? = file.findElementAt(offset)
                val method = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod::class.java)

                method?.let {
                    TestRunnerUtil.runRelatedTests(it)
                } ?: run {
                    Messages.showMessageDialog(
                        event.project,
                        "No method at caret position.",
                        "Information",
                        Messages.getInformationIcon()
                    )
                }
            } ?: run {
                Messages.showMessageDialog(
                    event.project,
                    "Editor not available.",
                    "Information",
                    Messages.getInformationIcon()
                )
            }
        } ?: run {
            Messages.showMessageDialog(
                event.project,
                "File not available.",
                "Information",
                Messages.getInformationIcon()
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        val psiFile = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE)

        val method = editor?.let { editor ->
            psiFile?.let { file ->
                val offset = editor.caretModel.offset
                val elementAtCaret: PsiElement? = file.findElementAt(offset)
                PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod::class.java)
            }
        }

        // Set visible if a method is found at caret
        e.presentation.isEnabledAndVisible = method != null
    }
}
```

테스트를 수행하기 위해 이렇게 작성해 주었다.
저기서 상속받고 있는 AnAction은 IntelliJ에서 사용자 수행 명령을 정의한다.

아마 코드를 보면 알겠지만, project 정보나 method에 대해 받아와서 돌리는 것이다.
얘는 사실상 컨텍스트 확인과 각 플로우를 어떻게 할지에 대한 대략적인 동작이고(Spring에서 Controller같은 것이라 보면 될듯.) 실제 로직에 대한 구현은

- TestRunnerUtil.kt

```
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod
import com.intellij.openapi.ui.Messages
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil

object TestRunnerUtil {

    fun runRelatedTests(method: PsiMethod) {
        val project = method.project
        val relatedTests = findAllRelatedTests(method)

        if (relatedTests.isEmpty()) {
            Messages.showMessageDialog(
                project,
                "No related tests found for this method.",
                "Information",
                Messages.getInformationIcon()
            )
            return
        }

        runMultipleTests(project, relatedTests)
    }

    private fun runMultipleTests(project: Project, testMethods: Set<PsiMethod>) {
        val runManager = RunManager.getInstance(project)
        val junitConfigurationType = ConfigurationTypeUtil.findConfigurationType(JUnitConfigurationType::class.java)
        val configurationFactory = junitConfigurationType.configurationFactories.firstOrNull()
            ?: throw IllegalStateException("JUnit configuration factory not found")

        val settings = runManager.createConfiguration("Run All Related Tests", configurationFactory)
        val configuration = settings.configuration as JUnitConfiguration
        val data = configuration.persistentData

        val module = testMethods.firstOrNull()?.let {
            ModuleUtil.findModuleForPsiElement(it)
        } ?: throw IllegalStateException("Cannot find module for test methods")

        configuration.setModule(module)

        val methodPatterns = testMethods.mapNotNull { method ->
            val className = method.containingClass?.qualifiedName ?: return@mapNotNull null
            "$className,${method.name}"
        }.toCollection(LinkedHashSet())

        data.apply {
            TEST_OBJECT = JUnitConfiguration.TEST_PATTERN
            PACKAGE_NAME = ""
            METHOD_NAME = ""
            MAIN_CLASS_NAME = ""
        }
        data.setPatterns(methodPatterns)

        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings

        val executor = DefaultRunExecutor.getRunExecutorInstance()
        val environment = ExecutionEnvironmentBuilder.create(executor, settings).build()
        ProgramRunnerUtil.executeConfiguration(environment, false, true)
    }

    private fun findAllRelatedTests(
        method: PsiMethod,
        visitedMethods: Set<PsiMethod> = emptySet()
    ): Set<PsiMethod> {
        if (method in visitedMethods) {
            return emptySet()
        }

        val updatedVisited = visitedMethods + method
        val currentMethodTests = if (isTestAnnotated(method)) {
            setOf(method)
        } else {
            emptySet()
        }

        val references = ReferencesSearch.search(method).findAll()

        val referencedTests = references
            .mapNotNull { reference ->
                findEnclosingMethod(reference)
            }
            .flatMap { enclosingMethod ->
                findAllRelatedTests(enclosingMethod, updatedVisited)
            }
            .toSet()

        return currentMethodTests + referencedTests
    }

    private fun findEnclosingMethod(reference: PsiReference): PsiMethod? {
        val element = reference.element

        val enclosingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        return enclosingMethod
    }

    private fun isTestAnnotated(method: PsiMethod): Boolean {
        return method.annotations.any {
            it.qualifiedName == "org.junit.jupiter.api.Test" ||
                    it.qualifiedName == "org.junit.Test"
        }
    }
}
```

여기서 해 준다.

runRelatedTest는 모든 테스트를 찾아서 있으면 동작시키고 없으면 알려주는 부분이다.
그리고 그 모든 테스트를 찾는 로직은 findAllRelatedTests 에서 진행되는데, **특정 메서드가 사용된 모든 사용처의 테스트를 찾는다** 라는 것에서 느낌이 올 테지만 결국은 최종 확인처는 재귀로 찾아가야 한다.

![image](https://github.com/user-attachments/assets/7976119a-9e36-4377-b059-ad53f01a7578)

이런 느낌을 생각해보면 이해가 쉬울 것이다.
즉 마지막까지 가면서 Test 여부를 봐야하는 것이다.

참고로 나는 Set을 썼는데, List로 하려고 했지만 생각해보니 도달점이 같은 테스트일 수도 있어 보였다.

재귀는 enclosingMethod 함수를 활용해 어떤 메서드의 내부에서 이게 호출되었는지 확인하고, 그 메서드로 다시 한번 테스트를 쭈루룩 찾아주는 것이다.

그리고 이게 내가 원하는 도달점이라면(isTestAnnotated) 그 테스트 정보를 저장해서 보내주면 된다.

이렇게 원하는 테스트 셋을 받았으면 그냥 runMultipleTests 으로 실행해주면 된다.

이상이 대략적인 로직에 관한 설명이다.
보면 알겠지만 사실 Action 등을 제외하고는 kotlin 구현 정도일 뿐이다.
처음에 plugin과 Action 사용법에 대해서만 알고 있다면 여기까지 만드는게 전혀 어렵지 않을 것이다.
