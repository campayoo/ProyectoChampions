<div align="center">

# ⚽ Proyecto Champions

### Simulador avanzado de torneos de fútbol en Java

<br/>

> Aplicación Java que simula un entorno completo de fútbol profesional.
> Gestión de equipos, jugadores, fichajes y torneos eliminatorios bajo una arquitectura sólida.

<br/>

[📖 Descripción](#-descripción) · [⚙️ Funcionalidades](#️-funcionalidades) · [🏗️ Arquitectura](#️-arquitectura) · [🚀 Instalación](#-instalación) · [🧪 Tests](#-tests)

</div>

---

## 📌 Descripción

**Proyecto Champions** es una aplicación Java que simula un entorno completo de fútbol profesional. Permite gestionar equipos, jugadores, realizar fichajes y organizar torneos eliminatorios, todo bajo una arquitectura sólida y una interfaz gráfica funcional construida con Swing.

---

## 📊 Métricas rápidas

| Indicador | Valor |
|-----------|-------|
| 🧪 Cobertura de tests | **85%** |
| ☕ Versión de Java | **17+** |
| 🏛️ Capas de arquitectura | **3** (Model · GUI · Data) |
| 🔌 Interfaces definidas | **2** (`Simulable` · `Transferible`) |
| 📁 Módulos principales | **5** |

---

## ⚙️ Funcionalidades

| Módulo | Descripción |
|--------|-------------|
| 🧍 **Jugadores** | Perfiles con atributos personalizados y sistema de transferencias |
| 🏟️ **Equipos** | Plantillas completas, gestión de presupuesto y entrenadores asociados |
| 💰 **Mercado** | Sistema dinámico de compra/venta con validación automática de presupuesto |
| ⚽ **Partidos** | Simulación automática con resultados basados en estadísticas de los equipos |
| 🏆 **Torneos** | Sistema de eliminatorias con rondas automáticas hasta el campeón final |

---

## 🧠 Conceptos aplicados

| Concepto | Descripción |
|----------|-------------|
| **POO** | Encapsulación, herencia y polimorfismo aplicados al dominio deportivo |
| **Interfaces** | Contratos definidos con `Simulable` y `Transferible` |
| **Arquitectura por capas** | Separación clara de responsabilidades entre Modelo y Vista |
| **Persistencia CSV** | Carga de datos inicial desde ficheros `.csv` al arrancar la aplicación |
| **GUI con Swing** | Interfaz gráfica con paneles y ventanas en Java Swing |

---

## 🏗️ Arquitectura

### 🧩 Estructura del proyecto

```
Proyecto_Champions/
│
├── 📂 data/                    # Datos externos
│   ├── equipos.csv
│   └── LectorDatos.java
│
├── 📂 gui/                     # Capa de presentación (Swing)
│   ├── MainFrame.java
│   ├── Panel*.java             # Paneles: Bienvenida, Mercado, Campo...
│   └── PanelCampo.java
│
├── 📂 interfaces/              # Contratos y abstracciones
│   ├── Simulable.java
│   └── Transferible.java
│
├── 📂 model/                   # Lógica de negocio y entidades
│   ├── Persona.java            # Clase padre
│   ├── Jugador.java
│   ├── Equipo.java
│   ├── Partido.java
│   └── MercadoFichajes.java
│
└── Main.java                   # Punto de entrada
```

### 🔄 Flujo del sistema

```
Inicio
  │
  ▼
Carga de datos (CSV)
  │
  ▼
Gestión de equipos y jugadores
  │
  ▼
Mercado de fichajes
  │
  ▼
Simulación de partidos
  │
  ▼
Torneo eliminatorio
  │
  ▼
🏆 Resultado final
```

---

## 🔌 Interfaces clave

### `Simulable`

```java
public interface Simulable {
    void simular();
}
```

### `Transferible`

```java
public interface Transferible {
    void transferir(Equipo destino);
}
```

---

## 🚀 Instalación

### Requisitos previos

- Java **17** o superior
- IntelliJ IDEA / Eclipse (recomendado)

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/proyecto-champions.git

# 2. Entrar al directorio
cd proyecto-champions

# 3. Ejecutar la aplicación
java Main.java
```

> [!NOTE]
> El archivo `data/equipos.csv` inicializa los equipos automáticamente al arrancar.

---

## 🧪 Tests

### Tecnologías

- **JUnit 5** — framework principal de testing
- **Mockito** *(opcional)* — mocking de dependencias
- **JaCoCo / IntelliJ** — generación de informes de cobertura

### Cobertura actual

```
Overall Coverage: 85%
████████████████░░░  85%
```

### Ejemplo de test

```java
@Test
void testSimulacionPartido() {
    Partido partido = new Partido(equipo1, equipo2);
    partido.simular();
    assertNotNull(partido.getResultado());
}
```

---

## 💡 Mejoras futuras

- [ ] 🗄️ Base de datos relacional (MySQL)
- [ ] 🌐 API REST con Spring Boot
- [ ] 🤖 IA para decisiones de juego
- [ ] 🎮 Modo carrera
- [ ] 👥 Multijugador (multiplayer)
- [ ] 🖼️ Migración de GUI a JavaFX

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Para cambios importantes, abre primero un issue para discutir qué te gustaría cambiar.

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

**Pablo** — Proyecto académico avanzado orientado a simulación deportiva y arquitectura software.

---

## 📄 Licencia

Uso educativo. Libre para modificar y distribuir.

---

<div align="center">

### ✔️ Arquitectura limpia · ✔️ Simulación realista · ✔️ Interfaz gráfica · ✔️ Buenas prácticas

*Hecho con ☕ Java y pasión por el fútbol*

</div>
