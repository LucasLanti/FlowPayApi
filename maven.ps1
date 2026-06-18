param (
    [string]$Command
)

switch ($Command) {
    "build" {
        ./mvnw.cmd clean install
    }
    "run" {
        ./mvnw.cmd spring-boot:run
    }
    default {
        Write-Host "Comando inválido. Use 'build' ou 'run'."
        Write-Host "Exemplo: .\maven build"
    }
}

