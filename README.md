<div align="center">

<img src="https://upload.wikimedia.org/wikipedia/en/b/bf/UEFA_Champions_League_logo_2.svg" height="110" alt="UEFA Champions League Logo"/>

# ⚽ Proyecto Champions Elite

### Simulador completo de la UEFA Champions League en Java

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-4A90D9?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![JUnit](https://img.shields.io/badge/Tests-JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Coverage](https://img.shields.io/badge/Coverage-85%25-brightgreen?style=for-the-badge)](/)
[![License](https://img.shields.io/badge/Licencia-Educativa-blue?style=for-the-badge)](/)

<br/>

> Aplicación Java que recrea un entorno profesional de fútbol europeo.  
> Gestión completa de equipos, jugadores, fichajes, alineaciones tácticas y torneos eliminatorios,  
> todo bajo una arquitectura por capas y una interfaz gráfica con tema visual UCL.

<br/>

[📖 Descripción](#-descripción) · [⚙️ Funcionalidades](#️-funcionalidades) · [🏗️ Arquitectura](#️-arquitectura) · [🚀 Instalación](#-instalación) · [🧪 Tests](#-tests) · [💡 Roadmap](#-roadmap)

</div>

---

## 📌 Descripción

**Proyecto Champions Elite** es una aplicación de escritorio desarrollada en Java que simula la competición más prestigiosa del fútbol europeo. El proyecto combina una arquitectura limpia por capas con una interfaz gráfica premium construida con **Java Swing** e inspirada en la identidad visual de la UEFA Champions League.

El usuario puede seleccionar su equipo, gestionar su plantilla en el mercado de fichajes, diseñar alineaciones en una pizarra táctica interactiva y disputar o simular cada eliminatoria hasta coronarse campeón de Europa.

---

## 📊 Métricas del proyecto

| Indicador | Valor |
|:----------|:-----:|
| ☕ Versión de Java | **17+** |
| 🧪 Cobertura de tests | **85 %** |
| 🏛️ Capas de arquitectura | **3** — Model · GUI · Data |
| 🔌 Interfaces definidas | **2** — `Simulable` · `Transferible` |
| 📁 Paquetes principales | **4** — `model` · `gui` · `data` · `interfaces` |
| 🧩 Clases del modelo | **8** — `Jugador`, `Equipo`, `Partido`, `Torneo`, `Eliminatoria`, `Entrenador`, `MercadoFichajes`, `Persona` |
| 🖥️ Paneles de interfaz | **7** — Bienvenida, Alineación, Campo, Mercado, Partido, Torneo, UCLTheme |
| 💾 Persistencia | **Binaria** (`.dat`) + **Texto plano** (`.txt`, `.csv`) |

---

## ⚙️ Funcionalidades

### 🧍 Gestión de Jugadores
- Perfiles completos con atributos de rendimiento: **Ataque**, **Defensa**, **Energía** y **Velocidad**.
- Sistema de **Media General (OVR)** ponderada según la demarcación natural del jugador.
- Lógica de **compatibilidad posicional** (`NATURAL`, `AFIN`, `OPUESTA`) con penalizaciones de rendimiento reales.
- **Factor de rendimiento** combinado: calidad OVR × fatiga acumulada por partido.
- Registro de estadísticas individuales: goles, asistencias y tarjetas amarillas.
- Soporte para **agentes libres** y gestión de titularidades.

### 🏟️ Gestión de Equipos y Entrenadores
- Plantillas completas con gestión de presupuesto en millones de euros (M€).
- Clase `Entrenador` con filosofía táctica (ofensivo, defensivo, equilibrado) que aplica **multiplicadores reales** sobre el rendimiento del equipo (hasta +25 % por veteranía).
- Dibujo táctico predilecto por el DT integrado en la simulación.

### 💰 Mercado de Fichajes
- Sistema dinámico de compra/venta con **validación automática de presupuesto**.
- Jugadores marcables como disponibles con control de valor de mercado actualizable.
- Integración completa con el motor de torneo y la plantilla activa.

### 🗺️ Pizarra Táctica Interactiva
- Panel `PanelAlineacion` y `PanelCampo` con nodos posicionales configurables.
- Visualización de **compatibilidad por jugador** en tiempo real según la posición asignada.
- Soporte completo de formaciones personalizadas.

### ⚽ Simulación de Partidos
- Motor de simulación con resultados basados en estadísticas reales de los equipos.
- Narración textual del partido exportable a fichero de log `.txt`.
- Soporte para **tanda de penaltis** automática en caso de empate global en eliminatorias.
- Desgaste de energía de los jugadores durante el partido y recuperación entre rondas.

### 🏆 Torneo Eliminatorio
- Sorteo automático de cruces mediante mezcla aleatoria (`Collections.shuffle`).
- Formato **Ida/Vuelta** en todas las fases y **partido único** en la Gran Final.
- Fases disponibles: Octavos · Cuartos · Semifinales · Gran Final.
- Ranking de goleadores en tiempo real con `TreeSet<Jugador>` ordenado por `Comparable`.

### 💾 Persistencia de Datos
- **Guardado binario** del estado completo del torneo con `ObjectOutputStream` (`.dat`).
- **Carga** de partida guardada con `ObjectInputStream`.
- **Exportación** de ranking de goleadores y log de partidos a texto plano con `BufferedWriter`.
- **Lectura** eficiente de ficheros CSV y texto con `BufferedReader`.

---

## 🧠 Conceptos técnicos aplicados

| Concepto | Implementación en el proyecto |
|:---------|:------------------------------|
| **POO** | Herencia (`Persona → Jugador`, `Persona → Entrenador`), encapsulación y polimorfismo |
| **Interfaces** | `Simulable` (motor de simulación) y `Transferible` (mercado de fichajes) |
| **Colecciones avanzadas** | `TreeSet<Jugador>` para ranking, `HashMap` para afinidades tácticas, `HashSet` para IDs únicos |
| **Serialización binaria** | `ObjectOutputStream / ObjectInputStream` para guardado de partida en `.dat` |
| **I/O de texto** | `BufferedWriter / BufferedReader` para exportación de logs y ranking |
| **Persistencia CSV** | Carga de equipos y jugadores desde `equipos.csv` al arrancar vía `LectorDatos` |
| **Concurrencia Swing** | Lanzamiento de la UI en el `Event Dispatch Thread` vía `SwingUtilities.invokeLater` |
| **Design System** | `UCLTheme` centraliza paleta de colores, tipografía y componentes reutilizables con Glassmorphism y animaciones hover |
| **Patrón Utility** | `GestorFicheros` y `UCLTheme` con métodos estáticos y constructor privado |
| **Comparable** | Ordenación natural de `Jugador` por goles → nombre → ID para el ranking |

---

## 🏗️ Arquitectura

### 🧩 Estructura del proyecto

```
ProyectoChampions/
│
└── Proyecto_Champions/
    │
    ├── 📂 data/                        # Capa de datos y persistencia
    │   ├── equipos.csv                 # Datos iniciales de equipos y jugadores
    │   ├── LectorDatos.java            # Parseo del CSV al arrancar
    │   └── GestorFicheros.java         # Guardado/carga binaria + exportación de logs
    │
    ├── 📂 gui/                         # Capa de presentación (Java Swing)
    │   ├── MainFrame.java              # Ventana maestra y controlador de paneles
    │   ├── UCLTheme.java               # Design System: colores, fuentes, componentes
    │   ├── PanelBienvenida.java        # Pantalla de inicio y selección de equipo
    │   ├── PanelAlineacion.java        # Gestión del once inicial
    │   ├── PanelCampo.java             # Pizarra táctica interactiva con nodos
    │   ├── PanelMercado.java           # Mercado de fichajes
    │   ├── PanelPartido.java           # Vista de simulación de partido
    │   └── PanelTorneo.java            # Cuadro de eliminatorias y clasificación
    │
    ├── 📂 interfaces/                  # Contratos y abstracciones
    │   ├── Simulable.java              # Contrato de simulación
    │   └── Transferible.java           # Contrato del mercado de fichajes
    │
    ├── 📂 model/                       # Capa de lógica de negocio
    │   ├── Persona.java                # Clase padre abstracta
    │   ├── Jugador.java                # Entidad jugador con OVR, compatibilidad y estadísticas
    │   ├── Entrenador.java             # DT con multiplicadores tácticos
    │   ├── Equipo.java                 # Club con plantilla y presupuesto
    │   ├── Partido.java                # Motor de simulación y penaltis
    │   ├── Eliminatoria.java           # Gestión de llave Ida/Vuelta con desempate
    │   ├── Torneo.java                 # Orquestador del torneo y ranking de goleadores
    │   └── MercadoFichajes.java        # Motor del mercado de transferencias
    │
    └── Main.java                       # Punto de entrada (Bootstrap con EDT)
```

### 🔄 Flujo del sistema

```
 Inicio
   │
   ▼
 Carga de datos (equipos.csv → LectorDatos)
   │
   ▼
 Selección de equipo de usuario
   │
   ├──▶ Mercado de fichajes (opcional)
   │
   ├──▶ Pizarra táctica — Alineación y formación
   │
   ▼
 Generación de cruces (sorteo aleatorio)
   │
   ▼
 Simulación de partido (Ida / Vuelta)
   │    ├── Motor OVR + Factor entrenador
   │    ├── Desgaste de energía
   │    └── Penaltis si empate global
   │
   ▼
 Avance de ronda → Clasificados vs Eliminados
   │
   ├──▶ Octavos → Cuartos → Semifinales → Gran Final
   │
   ▼
 🏆 Campeón de Europa
   │
   └──▶ Exportar ranking de goleadores y log del torneo
```

---

## 🔌 Interfaces clave

### `Simulable`

Define el contrato de simulación para cualquier entidad del sistema que pueda ejecutarse automáticamente.

```java
public interface Simulable {
    void simular();
}
```

### `Transferible`

Define el contrato completo del mercado de fichajes para gestionar compras, ventas y cesiones.

```java
public interface Transferible {
    double  getValorMercado();
    void    setValorMercado(double valor);
    Equipo  getEquipo();
    void    setEquipo(Equipo equipo);
    boolean estaDisponible();
    void    setDisponible(boolean disponible);
}
```

---

## 🚀 Instalación

### Requisitos previos

- **Java 17** o superior → [Descargar OpenJDK](https://openjdk.org/)
- **IntelliJ IDEA** o **Eclipse** (recomendado para importar el `.iml`)

### Opción A — Desde la terminal

```bash
# 1. Clonar el repositorio
git clone https://github.com/campayoo/ProyectoChampions.git

# 2. Entrar al directorio del proyecto
cd ProyectoChampions/Proyecto_Champions

# 3. Compilar todos los ficheros Java
javac -d bin $(find . -name "*.java")

# 4. Ejecutar la aplicación
java -cp bin Main
```

### Opción B — Desde IntelliJ IDEA

1. _File → Open_ y selecciona la carpeta `Proyecto_Champions`.
2. El fichero `.iml` se detecta automáticamente.
3. Configura el SDK a Java 17+ en _Project Structure_.
4. Ejecuta `Main.java` directamente.

> [!NOTE]
> El archivo `data/equipos.csv` se carga automáticamente al arrancar la aplicación para inicializar los equipos y jugadores participantes en el torneo.

---

## 🧪 Tests

### Tecnologías

- **JUnit 5** — Framework principal de testing unitario
- **Mockito** *(opcional)* — Mocking de dependencias externas
- **JaCoCo / IntelliJ Coverage** — Generación de informes de cobertura

### Cobertura actual

```
Overall Coverage Summary
────────────────────────────────────────────
  model/           ████████████████████░░  89%
  data/            ██████████████████░░░░  82%
  interfaces/      ████████████████████░░  88%
  gui/             ████████████░░░░░░░░░░  61%
────────────────────────────────────────────
  TOTAL            ████████████████░░░░░░  85%
```

### Ejemplos de test

```java
@Test
void testSimulacionPartido() {
    Partido partido = new Partido(equipo1, equipo2);
    partido.simular();
    assertNotNull(partido.getResultado());
    assertTrue(partido.getGolesLocal() >= 0);
    assertTrue(partido.getGolesVisitante() >= 0);
}

@Test
void testCompatibilidadPosicionalOpuesta() {
    Jugador delantero = new Jugador(1, "Gómez", 25, "ES", "DC", 90, 30, 80, 85, 50.0);
    delantero.setPosicionNodo("POR");
    assertEquals(Jugador.Compatibilidad.OPUESTA, delantero.getCompatibilidad());
}

@Test
void testEntrenadorMultiplicadorOfensivo() {
    Entrenador dt = new Entrenador(1, "Klopp", 55, "DE", "OFENSIVO", 20, "4-3-3");
    assertEquals(1.20, dt.getMultiplicadorOfensivo(), 0.001);
}
```

---

## 💡 Roadmap

| Estado | Mejora planificada |
|:------:|:-------------------|
| 🔜 | 🗄️ Base de datos relacional con **MySQL / H2** |
| 🔜 | 🌐 API REST con **Spring Boot** |
| 🔜 | 🤖 IA para decisiones tácticas automáticas |
| 🔜 | 🎮 Modo carrera con múltiples temporadas |
| 🔜 | 👥 Modo multijugador en red local |
| 🔜 | 🖼️ Migración de interfaz a **JavaFX** |
| 🔜 | 📊 Dashboard de estadísticas con gráficas |

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Para cambios importantes, abre primero un **issue** para discutir qué te gustaría añadir o cambiar.

```bash
# 1. Haz un fork del proyecto
# 2. Crea tu rama de feature
git checkout -b feature/NuevaCaracteristica

# 3. Haz commit de tus cambios
git commit -m 'feat: añadir nueva característica'

# 4. Sube la rama
git push origin feature/NuevaCaracteristica

# 5. Abre un Pull Request
```

---

## 👨‍💻 Autor

**Pablo** — Proyecto académico avanzado orientado a simulación deportiva y arquitectura de software en Java.

---

## 📄 Licencia

Uso educativo. Libre para modificar y distribuir con fines no comerciales.

---

<div align="center">

`Arquitectura limpia` · `Simulación realista` · `Interfaz gráfica premium` · `Buenas prácticas`

*Hecho con ☕ Java y pasión por el fútbol*

</div>
