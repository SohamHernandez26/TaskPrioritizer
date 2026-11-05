# Entrega 4: flujo funcional de TaskPrioritizer

El flujo de registro, consulta y finalización de tareas deja de depender de MockData y pasa por ViewModels y repositorios. Integra autenticación por correo, tareas en Firestore, recordatorios locales en Room, formulario validado y estados visibles de carga, error, vacío y sincronización pendiente.

> BORRADOR: completar todos los campos PENDIENTE con evidencia real antes de entregar. No afirmar pruebas que no se realizaron.

## Identificación

- Santiago Estuardo Maldonado Garrido — 25717.
- Gabriel Zambrano Garoz — 25676.
- Zabdi Soham Pelicó Hernández — 25586.
- SHA del último commit evaluable: **PENDIENTE: pegar salida de `git rev-parse HEAD` después del último commit**.
- Rama: `entrega-4` → `main`. El PR debe quedar abierto, sin fusionar.

## Video

**PENDIENTE: enlace accesible al video continuo, sin cortes, de máximo 3 minutos.**

Debe mostrar, en este orden, el flujo que escribe en Firestore, el documento en la consola, la lectura y escritura en modo avión y, todavía sin red, el cierre/reapertura con el recordatorio de Room visible.

## Task test

- Persona y relación con el equipo: **PENDIENTE; debe ser alguien externo real**.
- Tarea indicada: “Registra una actividad que tengas pendiente, revisa dónde aparece en tu plan y programa un recordatorio para ella”. Ajustar si se probó otra tarea.
- ¿La completó?: **PENDIENTE: sí / con dificultad / no**.
- Dónde dudó o se equivocó, describiendo lo que hizo: **PENDIENTE**.
- Cambio que haremos a partir de lo observado: **PENDIENTE**.
- Confirmación de que no recibió ayuda durante el intento: **PENDIENTE**.

## Alcance congelado

**PENDIENTE: copiar los IDs y textos oficiales de las user stories de Entrega 2 y ubicarlos en Dentro/Fuera. No se adjuntó ese documento, así que no se inventaron sus historias ni identificadores.**

### Dentro

Relacionar las historias oficiales con estas capacidades implementadas:

- Registro e inicio de sesión por correo/contraseña.
- Registrar actividades con nombre, prioridad, tiempo estimado, fecha y notas.
- Consultar actividades y su detalle.
- Ver un orden recomendado por prioridad y fecha.
- Marcar actividades como completadas.
- Guardar y consultar recordatorios locales de 30 minutos, posponerlos o eliminarlos.
- Consultar datos disponibles y dejar escrituras pendientes cuando falta conexión.

### Fuera

**PENDIENTE: lista de historias oficiales que no se harán, con una línea de razón por cada una.**

Para decidir con el documento original: esta versión no incluye edición/eliminación de tareas desde la UI, alarmas con la app cerrada, notificaciones push, categorías personalizadas, calendario externo, perfiles editables ni estimación automática por historial. No presentar esta enumeración como si fueran las historias originales.

## Observaciones atendidas

**PENDIENTE: no se proporcionaron las observaciones escritas del docente. Completar la tabla con las reales o indicar que no hubo observaciones únicamente si el equipo lo confirma.**

| Observación del docente (enlace o cita breve) | Qué se hizo o justificación | Commit |
|---|---|---|
| PENDIENTE | PENDIENTE | PENDIENTE |

## Uso de IA

Se utilizó ChatGPT como apoyo para comparar las versiones de main y Santiago, identificar faltantes frente a la rúbrica y preparar la integración de repositorios, ViewModels, navegación, pantallas, manejo sin conexión, recordatorios Room, reglas de Firestore y pruebas. También asistió la documentación de entrega y este borrador de PR.

En el entorno de asistencia se ejecutaron verificaciones estáticas de estructura, imports, modelos y formatos, y comprobaciones del SQL con SQLite. No fue posible compilar Android ni ejecutar pruebas JUnit/instrumentadas o Firebase real en ese entorno.

**Verificación realizada por el equipo: PENDIENTE: completar resultados reales de compilación, pruebas, modo avión, rotación, task test y revisión del código.** El equipo debe comprender y poder defender el código presentado.

## Validación técnica

- Compilación debug: **PENDIENTE**.
- 9 pruebas unitarias: **PENDIENTE**.
- 2 pruebas instrumentadas de Room: **PENDIENTE**.
- Firebase real y reglas publicadas: **PENDIENTE**.
- Modo avión y reapertura con Room: **PENDIENTE**.
- Rotación y navegación Atrás: **PENDIENTE**.

## Notas de implementación

- Los Composables de contenido reciben estado/callbacks; los ViewModels reciben repositorios por constructor.
- Las escrituras tienen una espera acotada para no dejar cargando a la app cuando Firestore espera conexión.
- “Pendiente de confirmación” no se presenta como escritura ya aceptada por el servidor.
- Los recordatorios son locales y visibles dentro de la app, no notificaciones del sistema con la app cerrada.
- La fecha no pasada se valida al crear; las reglas del servidor validan su formato para permitir que una escritura offline llegue después de la fecha de entrega.
