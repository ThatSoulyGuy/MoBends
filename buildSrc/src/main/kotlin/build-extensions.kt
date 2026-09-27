import org.gradle.api.Project
import org.gradle.kotlin.dsl.expand
import org.gradle.language.jvm.tasks.ProcessResources

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()
fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

fun ProcessResources.properties(files: Iterable<String>, vararg properties: Pair<String, Any>) {
    for ((name, value) in properties) inputs.property(name, value)
    filesMatching(files) {
        expand(properties.toMap())
    }
}

/**
 * Value the root gradle.properties uses to declare "this key exists, but its value is
 * per-Minecraft-version". It is a marker, never a usable value: seeing one at configuration
 * time means some versions/<version>/gradle.properties failed to override it.
 */
const val VERSIONED_PLACEHOLDER = "[VERSIONED]"

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = required("mod.id")
    val name: String get() = required("mod.name")
    val version: String get() = required("mod.version")

    fun prop(key: String) = required("mod.$key")
    fun dep(key: String) = required("deps.$key")

    /**
     * Reads a property, failing the build if it is missing OR still the [VERSIONED_PLACEHOLDER].
     *
     * Without the placeholder check a missing per-version override is not an error: the literal
     * string "[VERSIONED]" satisfies requireNotNull and gets templated straight into mods.toml,
     * producing a jar that only fails at mod-load time on a user's machine.
     */
    private fun required(key: String): String {
        val value = requireNotNull(project.prop(key)) {
            "Missing '$key' while configuring '${project.path}'. Declare it in gradle.properties, " +
                "or in versions/<version>/gradle.properties if it differs per Minecraft version."
        }
        require(value != VERSIONED_PLACEHOLDER) {
            "'$key' is still the '$VERSIONED_PLACEHOLDER' placeholder while configuring '${project.path}'. " +
                "The root gradle.properties only declares that this key is per-version — give it a real " +
                "value in the matching versions/<version>/gradle.properties, or drop the placeholder if " +
                "the key is no longer used."
        }
        return value
    }
}
