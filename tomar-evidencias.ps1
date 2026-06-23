# Script de evidencias - API Blueprints (perfil postgres)
# Recorre el camino feliz y los casos de error mostrando codigo HTTP + body.
# Uso:  .\tomar-evidencias.ps1

$base = "http://localhost:8080/api/v1/blueprints"

function Llamar($titulo, $metodo, $url, $body) {
    Write-Host ""
    Write-Host "=== $titulo ===" -ForegroundColor Cyan
    Write-Host "$metodo $url" -ForegroundColor Yellow
    try {
        $params = @{ Method = $metodo; Uri = $url; UseBasicParsing = $true }
        if ($body) {
            $params.Body = $body
            $params.ContentType = "application/json"
            Write-Host "Body: $body" -ForegroundColor DarkGray
        }
        $r = Invoke-WebRequest @params
        Write-Host "HTTP $([int]$r.StatusCode) $($r.StatusDescription)" -ForegroundColor Green
        Write-Host $r.Content
    }
    catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $code = [int]$resp.StatusCode
            $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
            $contenido = $reader.ReadToEnd()
            Write-Host "HTTP $code $($resp.StatusDescription)" -ForegroundColor Red
            Write-Host $contenido
        } else {
            Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "#   SECCION 3: CAMINO FELIZ" -ForegroundColor Magenta

Llamar "3.1 Listar todo (BD vacia)" "GET" $base $null

Llamar "3.2 Crear blueprint (espera 201)" "POST" $base `
    '{"author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}]}'

Llamar "3.3 Consultar el creado" "GET" "$base/john/kitchen" $null

Llamar "3.4 Agregar un punto (espera 202)" "PUT" "$base/john/kitchen/points" `
    '{"x":9,"y":9}'

Llamar "3.5 Consultar tras agregar (3 puntos)" "GET" "$base/john/kitchen" $null

Write-Host ""
Write-Host "#   SECCION 4: CASOS DE ERROR" -ForegroundColor Magenta

Llamar "4.1 Autor inexistente (espera 404)" "GET" "$base/nadie" $null

Llamar "4.2 Blueprint inexistente (espera 404)" "GET" "$base/john/noexiste" $null

Llamar "4.3 Crear duplicado (espera 409)" "POST" $base `
    '{"author":"john","name":"kitchen","points":[{"x":1,"y":1}]}'

Llamar "4.4 Validacion: author en blanco (espera 400)" "POST" $base `
    '{"author":"","name":"sala","points":[{"x":1,"y":1}]}'

Write-Host ""
Write-Host "=== FIN. Ahora corre la consulta SQL para la seccion 5 ===" -ForegroundColor Cyan