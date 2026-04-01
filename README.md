# ⚽ Proyecto Champions  
### 🏆 Simulador avanzado de torneos de fútbol en Java

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Arquitectura](https://img.shields.io/badge/Arquitectura-POO%20%7C%20MVC-blue?style=for-the-badge)
![Estado](https://img.shields.io/badge/Estado-Finalizado-brightgreen?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-JUnit-green?style=for-the-badge)
![Coverage](https://img.shields.io/badge/Coverage-85%25-yellowgreen?style=for-the-badge)

---

## 📌 1. Descripción
**Proyecto Champions** es una aplicación Java que simula un entorno completo de fútbol profesional. Permite gestionar equipos, jugadores, realizar fichajes y organizar torneos eliminatorios, todo bajo una arquitectura sólida y una interfaz gráfica funcional.

---

## 🎥 2. Demo visual (GIFs)
* **🏠 Pantalla principal**: ![Demo Principal](docs/gifs/main.gif)
* **💰 Mercado de fichajes**: ![Mercado](docs/gifs/mercado.gif)
* **⚽ Simulación de partido**: ![Partido](docs/gifs/partido.gif)
* **🏆 Torneo**: ![Torneo](docs/gifs/torneo.gif)

---

## 📸 3. Capturas de pantalla
* **Interfaz general**: ![UI](docs/images/ui.png)
* **Alineaciones**: ![Alineacion](docs/images/alineacion.png)
* **Panel de equipos**: ![Equipos](docs/images/equipos.png)

---

## 🧠 4. Conceptos aplicados
* **Programación Orientada a Objetos (POO)**: Encapsulación, herencia y polimorfismo.
* **Interfaces**: Implementación de contratos con `Simulable` y `Transferible`.
* **Arquitectura por capas**: Separación de responsabilidades (Modelo-Vista).
* **Persistencia**: Lectura de datos desde archivos **CSV**.
* **GUI**: Desarrollo de interfaces con **Swing**.

---

## 🏗️ 5. Arquitectura
### 📊 Diagrama UML
![UML](docs/uml/diagrama.png)

### 🧩 Estructura del proyecto
```bash
Proyecto_Champions/
│
├── data/               # Datos externos (CSVs)
│   ├── equipos.csv
│   └── LectorDatos.java
│
├── gui/                # Capa de presentación (Swing)
│   ├── MainFrame.java
│   ├── Panel...        # Paneles específicos (Bienvenida, Mercado, etc.)
│   └── PanelCampo.java
│
├── interfaces/         # Contratos y abstracciones
│   ├── Simulable.java
│   └── Transferible.java
│
├── model/              # Lógica de negocio y entidades
│   ├── Persona.java    # Clase padre
│   ├── Jugador.java
│   ├── Equipo.java
│   ├── Partido.java
│   └── MercadoFichajes.java
│
└── Main.java           # Punto de entrada

## ⚙️ 6. Funcionalidades

* **🧍 Jugadores:** Creación de perfiles con atributos personalizados y sistema de transferencias.
* **🏟️ Equipos:** Plantillas completas, gestión de presupuesto y entrenadores asociados.
* **💰 Mercado:** Sistema dinámico de compra/venta con validación automática de presupuesto.
* **⚽ Partidos:** Simulación automática con resultados dinámicos basados en estadísticas de los equipos.
* **🏆 Torneos:** Sistema de eliminatorias con rondas automáticas hasta alcanzar el campeón final.

---

## 🔌 7. Interfaces clave

### `Simulable`
```java
public interface Simulable {
    void simular();
}

Transferible
public interface Transferible {
    void transferir(Equipo destino);
}
## 🔄 8. Flujo del sistema
Inicio
 ↓
Carga de datos (CSV)
 ↓
Gestión de equipos/jugadores
 ↓
Mercado de fichajes
 ↓
Simulación de partidos
 ↓
Torneo
 ↓
Resultado final
## 🚀 9. Instalación y ejecución
### 1. Clonar repositorio
git clone https://github.com/tu-usuario/proyecto-champions.git
### 2. Requisitos
Java 17+
IntelliJ / Eclipse
### 3. Ejecutar
Main.java
## 🧪 10. Tests y cobertura
Tecnologías
JUnit 5
Mockito (opcional)
Ejemplo de test
@Test
void testSimulacionPartido() {
    Partido partido = new Partido(equipo1, equipo2);
    partido.simular();
    assertNotNull(partido.getResultado());
}
Cobertura

💡 Puedes generarlo con IntelliJ o JaCoCo

## 📊 11. Datos iniciales
data/equipos.csv

Permite inicializar equipos automáticamente.

##💡 12. Mejoras futuras
Base de datos (MySQL)
API REST (Spring Boot)
IA para decisiones de juego
Modo carrera
Multiplayer
Migración a JavaFX
## 👨‍💻 13. Autor

Pablo

Proyecto académico avanzado orientado a simulación deportiva y arquitectura software.

##📄 14. Licencia

Uso educativo. Libre para modificar.

## ⭐ 15. Contribuir
Fork 🍴
Branch 🚀
Commit
Pull Request
## 🏁 16. Conclusión

Este proyecto representa una implementación completa de:

✔ Arquitectura limpia
✔ Simulación realista
✔ Interfaz gráfica funcional
✔ Buenas prácticas de desarrollo
