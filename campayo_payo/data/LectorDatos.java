package data;

import model.*;
import java.io.*;
import java.util.*;

/**
 * LectorDatos — carga equipos y jugadores desde un CSV o genera datos por defecto.
 *
 * Formato del CSV:
 *   EQUIPO,nombre,pais,presupuesto,nombreEntrenador,edadEnt,nacionEnt,estilo,exp,formacion
 *   JUGADOR,nombreEquipo,id,nombre,edad,nacion,posicion,ataque,defensa,energia,velocidad,valor
 */
public class LectorDatos {

    // ── Carga desde archivo CSV ───────────────────────────────────────────

    public static ArrayList<Equipo> cargarDesdeCSV(String ruta) throws IOException {
        ArrayList<Equipo>      equipos  = new ArrayList<>();
        Map<String, Equipo>    mapa     = new LinkedHashMap<>();
        BufferedReader         br       = new BufferedReader(new FileReader(ruta));
        String                 linea;
        boolean                header   = true;

        while ((linea = br.readLine()) != null) {
            if (header) { header = false; continue; }
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;
            String[] p = linea.split(",");
            try {
                if ("EQUIPO".equals(p[0].trim())) {
                    Equipo eq = new Equipo(p[1].trim(), p[2].trim(), Double.parseDouble(p[3].trim()));
                    Entrenador ent = new Entrenador(
                        mapa.size() * 100, p[4].trim(), Integer.parseInt(p[5].trim()),
                        p[6].trim(), p[7].trim(), Integer.parseInt(p[8].trim()), p[9].trim());
                    eq.setEntrenador(ent);
                    eq.setFormacion(p[9].trim());
                    mapa.put(p[1].trim(), eq);
                    equipos.add(eq);
                } else if ("JUGADOR".equals(p[0].trim())) {
                    Equipo eq = mapa.get(p[1].trim());
                    if (eq == null) continue;
                    Jugador j = new Jugador(
                        Integer.parseInt(p[2].trim()), p[3].trim(), Integer.parseInt(p[4].trim()),
                        p[5].trim(), p[6].trim(), Integer.parseInt(p[7].trim()),
                        Integer.parseInt(p[8].trim()), Integer.parseInt(p[9].trim()),
                        Integer.parseInt(p[10].trim()), Double.parseDouble(p[11].trim()));
                    eq.agregarJugador(j);
                }
            } catch (Exception e) {
                System.err.println("Error en línea: " + linea + " → " + e.getMessage());
            }
        }
        br.close();
        return equipos;
    }

    // ── Generación de datos por defecto (16 equipos UCL reales) ──────────

    public static ArrayList<Equipo> generarDatosDefecto() {
        ArrayList<Equipo> equipos = new ArrayList<>();
        Random rnd = new Random(2024L); // semilla fija → reproducible

        // ── Datos de los 16 equipos ──
        Object[][] clubes = {
            {"Real Madrid",          "España",          200.0, "Carlo Ancelotti",       62, "EQUILIBRADO", 25, "4-3-3"},
            {"FC Barcelona",         "España",          175.0, "Xavi Hernández",        44, "OFENSIVO",    8,  "4-3-3"},
            {"Manchester City",      "Inglaterra",      230.0, "Pep Guardiola",         53, "OFENSIVO",    16, "4-3-3"},
            {"Bayern Munich",        "Alemania",        195.0, "Thomas Tuchel",         50, "EQUILIBRADO", 12, "4-2-3-1"},
            {"Paris Saint-Germain",  "Francia",         220.0, "Luis Enrique",          53, "OFENSIVO",    10, "4-3-3"},
            {"Liverpool",            "Inglaterra",      180.0, "Jürgen Klopp",          56, "OFENSIVO",    16, "4-3-3"},
            {"Chelsea",              "Inglaterra",      160.0, "Mauricio Pochettino",   51, "EQUILIBRADO", 14, "4-2-3-1"},
            {"Atlético de Madrid",   "España",          145.0, "Diego Simeone",         54, "DEFENSIVO",   14, "4-4-2"},
            {"Juventus",             "Italia",          155.0, "Massimiliano Allegri",  56, "DEFENSIVO",   12, "3-5-2"},
            {"Inter Milan",          "Italia",          150.0, "Simone Inzaghi",        47, "EQUILIBRADO", 9,  "3-5-2"},
            {"AC Milan",             "Italia",          145.0, "Stefano Pioli",         58, "OFENSIVO",    10, "4-2-3-1"},
            {"Borussia Dortmund",    "Alemania",        140.0, "Edin Terzić",           41, "OFENSIVO",    6,  "4-2-3-1"},
            {"Ajax",                 "Países Bajos",    120.0, "John van 't Schip",     61, "OFENSIVO",    8,  "4-3-3"},
            {"Porto",                "Portugal",        115.0, "Sérgio Conceição",      49, "DEFENSIVO",   12, "4-4-2"},
            {"Napoli",               "Italia",          135.0, "Walter Mazzarri",       62, "EQUILIBRADO", 20, "4-3-3"},
            {"Benfica",              "Portugal",        110.0, "Roger Schmidt",         56, "OFENSIVO",    10, "4-2-3-1"},
        };

        // Plantillas: 1 POR + 4 DEF + 4 MED + 2 DEL = 11 jugadores base + 4 suplentes
        String[][][] plantillas = {
            {{"Thibaut Courtois","Bélgica","POR","82","90","80","65","75"},
             {"Dani Carvajal","España","DEF","72","84","83","85","60"},
             {"Eder Militao","Brasil","DEF","65","85","82","78","55"},
             {"Antonio Rüdiger","Alemania","DEF","62","88","83","75","50"},
             {"Ferland Mendy","Francia","DEF","68","82","80","82","50"},
             {"Luka Modrić","Croacia","MED","74","72","88","78","40"},
             {"Toni Kroos","Alemania","MED","76","68","87","72","40"},
             {"Federico Valverde","Uruguay","MED","79","75","85","84","80"},
             {"Aurélien Tchouaméni","Francia","MED","74","78","83","80","70"},
             {"Vinícius Jr","Brasil","DEL","90","45","88","96","100"},
             {"Joselu","España","DEL","80","50","80","72","30"},
             {"Rodrygo","Brasil","DEL","84","46","84","88","65"},
             {"Nacho","España","DEF","60","82","78","70","25"},
             {"Eduardo Camavinga","Francia","MED","76","72","82","80","60"},
             {"Brahim Díaz","España","DEL","78","46","80","82","35"}},

            {{"Marc-André ter Stegen","Alemania","POR","80","88","78","60","70"},
             {"Jules Koundé","Francia","DEF","74","83","80","83","65"},
             {"Alejandro Balde","España","DEF","70","80","79","86","45"},
             {"Íñigo Martínez","España","DEF","63","86","79","72","30"},
             {"Andreas Christensen","Dinamarca","DEF","64","84","78","72","40"},
             {"Pedri","España","MED","76","70","88","80","75"},
             {"Gavi","España","MED","74","72","86","80","65"},
             {"Frenkie de Jong","Países Bajos","MED","75","71","85","80","75"},
             {"İlkay Gündoğan","Alemania","MED","76","72","84","76","55"},
             {"Robert Lewandowski","Polonia","DEL","85","52","86","78","90"},
             {"Ferran Torres","España","DEL","80","50","82","85","55"},
             {"Ansu Fati","España","DEL","82","47","80","86","45"},
             {"João Félix","Portugal","DEL","83","48","82","84","60"},
             {"Raphinha","Brasil","DEL","82","46","83","87","60"},
             {"Sergi Roberto","España","MED","68","68","78","76","15"}},

            {{"Ederson","Brasil","POR","82","89","79","60","65"},
             {"Kyle Walker","Inglaterra","DEF","72","84","83","88","65"},
             {"Rúben Dias","Portugal","DEF","66","88","83","75","90"},
             {"Manuel Akanji","Suiza","DEF","65","86","82","76","55"},
             {"Joško Gvardiol","Croacia","DEF","67","85","83","78","60"},
             {"Kevin De Bruyne","Bélgica","MED","88","70","88","82","100"},
             {"Rodri","España","MED","76","82","88","76","90"},
             {"Bernardo Silva","Portugal","MED","82","72","87","84","80"},
             {"Phil Foden","Inglaterra","MED","85","68","86","86","90"},
             {"Erling Haaland","Noruega","DEL","92","42","86","90","150"},
             {"Julián Álvarez","Argentina","DEL","83","56","86","84","70"},
             {"Jack Grealish","Inglaterra","DEL","80","50","83","82","60"},
             {"Jeremy Doku","Bélgica","DEL","82","44","82","92","50"},
             {"Matheus Nunes","Portugal","MED","77","72","83","82","45"},
             {"Stefan Ortega","Alemania","POR","77","85","76","56","18"}},

            {{"Manuel Neuer","Alemania","POR","80","90","78","60","35"},
             {"Josip Stanišić","Croacia","DEF","70","80","79","82","30"},
             {"Matthijs de Ligt","Países Bajos","DEF","66","86","82","74","65"},
             {"Dayot Upamecano","Francia","DEF","67","85","82","76","65"},
             {"Alphonso Davies","Canadá","DEF","74","78","82","90","70"},
             {"Joshua Kimmich","Alemania","MED","78","80","88","78","80"},
             {"Leon Goretzka","Alemania","MED","77","78","86","80","55"},
             {"Leroy Sané","Alemania","MED","84","62","84","90","70"},
             {"Jamal Musiala","Alemania","MED","87","66","86","86","100"},
             {"Harry Kane","Inglaterra","DEL","88","52","87","78","100"},
             {"Thomas Müller","Alemania","DEL","76","62","82","76","35"},
             {"Serge Gnabry","Alemania","DEL","82","52","83","86","55"},
             {"Eric Maxim Choupo-Moting","Camerún","DEL","78","52","80","72","18"},
             {"Sven Ulreich","Alemania","POR","74","83","73","54","10"},
             {"Noussair Mazraoui","Marruecos","DEF","72","80","80","82","35"}},

            {{"Gianluigi Donnarumma","Italia","POR","80","88","76","60","60"},
             {"Achraf Hakimi","Marruecos","DEF","76","82","82","88","70"},
             {"Presnel Kimpembe","Francia","DEF","63","83","80","73","35"},
             {"Marquinhos","Brasil","DEF","68","82","85","75","70"},
             {"Nuno Mendes","Portugal","DEF","72","78","82","86","55"},
             {"Marco Verratti","Italia","MED","80","72","86","78","55"},
             {"Vitinha","Portugal","MED","80","72","85","78","60"},
             {"Fabián Ruiz","España","MED","78","70","83","76","50"},
             {"Ousmane Dembélé","Francia","DEL","86","46","84","90","80"},
             {"Kylian Mbappé","Francia","DEL","92","58","90","97","180"},
             {"Randal Kolo Muani","Francia","DEL","84","52","84","86","80"},
             {"Gonçalo Ramos","Portugal","DEL","82","52","84","80","60"},
             {"Lee Kang-in","Corea del Sur","MED","80","68","82","80","40"},
             {"Lucas Hernández","Francia","DEF","64","82","80","74","35"},
             {"Danilo","Brasil","DEF","70","80","80","80","25"}},

            {{"Alisson Becker","Brasil","POR","82","90","80","62","70"},
             {"Trent Alexander-Arnold","Inglaterra","DEF","78","82","84","82","80"},
             {"Virgil van Dijk","Países Bajos","DEF","68","88","86","78","80"},
             {"Ibrahima Konaté","Francia","DEF","68","84","82","78","60"},
             {"Andrew Robertson","Escocia","DEF","74","80","82","84","65"},
             {"Alexis Mac Allister","Argentina","MED","78","74","85","80","65"},
             {"Dominik Szoboszlai","Hungría","MED","80","72","86","82","70"},
             {"Harvey Elliott","Inglaterra","MED","78","68","83","82","40"},
             {"Mohamed Salah","Egipto","DEL","88","60","86","90","100"},
             {"Darwin Núñez","Uruguay","DEL","85","52","86","88","75"},
             {"Luis Díaz","Colombia","DEL","84","52","84","88","70"},
             {"Diogo Jota","Portugal","DEL","82","52","84","82","55"},
             {"Cody Gakpo","Países Bajos","DEL","82","52","84","84","55"},
             {"Wataru Endo","Japón","MED","73","78","82","74","25"},
             {"Caoimhín Kelleher","Irlanda","POR","75","83","74","55","15"}},

            {{"Robert Sánchez","España","POR","77","86","76","62","35"},
             {"Reece James","Inglaterra","DEF","74","83","82","84","60"},
             {"Thiago Silva","Brasil","DEF","64","86","84","68","25"},
             {"Levi Colwill","Inglaterra","DEF","66","82","80","74","35"},
             {"Ben Chilwell","Inglaterra","DEF","70","78","80","82","40"},
             {"Enzo Fernández","Argentina","MED","78","72","84","78","80"},
             {"Moisés Caicedo","Ecuador","MED","76","78","84","78","70"},
             {"Cole Palmer","Inglaterra","MED","84","64","85","84","80"},
             {"Raheem Sterling","Inglaterra","DEL","83","54","83","88","55"},
             {"Nicolas Jackson","Senegal","DEL","82","52","84","84","60"},
             {"Christopher Nkunku","Francia","DEL","84","54","84","82","80"},
             {"Conor Gallagher","Inglaterra","MED","74","74","83","78","40"},
             {"Noni Madueke","Inglaterra","DEL","80","50","82","86","40"},
             {"Malo Gusto","Francia","DEF","70","80","79","82","30"},
             {"Djordje Petrovic","Serbia","POR","74","83","73","56","15"}},

            {{"Jan Oblak","Eslovenia","POR","84","90","78","60","90"},
             {"Nahuel Molina","Argentina","DEF","72","80","82","84","50"},
             {"José María Giménez","Uruguay","DEF","64","86","82","73","50"},
             {"Axel Witsel","Bélgica","DEF","64","82","80","70","30"},
             {"Reinildo","Mozambique","DEF","68","82","80","78","25"},
             {"Koke","España","MED","74","72","86","76","50"},
             {"Rodrigo De Paul","Argentina","MED","76","74","86","78","70"},
             {"Saúl Ñíguez","España","MED","74","74","84","76","35"},
             {"Antoine Griezmann","Francia","DEL","84","64","86","80","85"},
             {"Álvaro Morata","España","DEL","80","56","84","78","50"},
             {"Ángel Correa","Argentina","DEL","78","58","83","80","40"},
             {"Memphis Depay","Países Bajos","DEL","82","52","82","82","25"},
             {"Geoffrey Kondogbia","Guinea Ecuatorial","MED","72","76","82","74","20"},
             {"Marcos Llorente","España","MED","76","74","83","80","55"},
             {"Stefan Savić","Montenegro","DEF","63","85","80","71","20"}},

            {{"Wojciech Szczęsny","Polonia","POR","79","88","77","60","50"},
             {"Danilo","Brasil","DEF","70","80","80","80","35"},
             {"Gleison Bremer","Brasil","DEF","66","85","82","74","65"},
             {"Federico Gatti","Italia","DEF","64","82","79","72","20"},
             {"Álex Sandro","Brasil","DEF","66","78","79","78","20"},
             {"Adrien Rabiot","Francia","MED","77","72","84","78","50"},
             {"Manuel Locatelli","Italia","MED","74","76","83","74","45"},
             {"Filip Kostić","Serbia","MED","76","68","82","80","35"},
             {"Federico Chiesa","Italia","DEL","84","52","83","86","70"},
             {"Dušan Vlahović","Serbia","DEL","86","52","85","78","90"},
             {"Moise Kean","Italia","DEL","80","52","82","78","30"},
             {"Arkadiusz Milik","Polonia","DEL","80","52","82","76","18"},
             {"Nicolò Fagioli","Italia","MED","76","68","81","76","25"},
             {"Mattia Perin","Italia","POR","75","85","74","56","15"},
             {"Andrea Cambiaso","Italia","DEF","72","78","80","80","30"}},

            {{"André Onana","Camerún","POR","80","87","78","62","60"},
             {"Denzel Dumfries","Países Bajos","DEF","74","78","82","86","45"},
             {"Stefan de Vrij","Países Bajos","DEF","64","84","82","72","40"},
             {"Alessandro Bastoni","Italia","DEF","67","78","84","76","70"},
             {"Federico Dimarco","Italia","DEF","74","76","82","82","50"},
             {"Nicolò Barella","Italia","MED","82","72","87","82","85"},
             {"Hakan Çalhanoğlu","Turquía","MED","82","72","86","78","70"},
             {"Henrikh Mkhitaryan","Armenia","MED","76","70","82","76","25"},
             {"Lautaro Martínez","Argentina","DEL","87","55","86","84","110"},
             {"Marcus Thuram","Francia","DEL","84","52","85","84","70"},
             {"Alexis Sánchez","Chile","DEL","78","56","80","80","15"},
             {"Joaquín Correa","Argentina","DEL","78","50","80","82","20"},
             {"Carlos Augusto","Brasil","DEF","68","78","80","80","30"},
             {"Yann Sommer","Suiza","POR","78","87","76","58","30"},
             {"Kristjan Asllani","Albania","MED","74","70","80","76","20"}},

            {{"Mike Maignan","Francia","POR","82","88","78","62","75"},
             {"Davide Calabria","Italia","DEF","70","80","81","82","35"},
             {"Malick Thiaw","Alemania","DEF","66","82","80","76","40"},
             {"Fikayo Tomori","Inglaterra","DEF","67","83","82","78","50"},
             {"Théo Hernández","Francia","DEF","74","80","82","86","65"},
             {"Tijjani Reijnders","Países Bajos","MED","80","70","84","80","60"},
             {"Ruben Loftus-Cheek","Inglaterra","MED","78","72","83","78","40"},
             {"Yunus Musah","Estados Unidos","MED","76","70","83","80","40"},
             {"Rafael Leão","Portugal","DEL","86","50","85","90","100"},
             {"Olivier Giroud","Francia","DEL","78","56","80","70","20"},
             {"Christian Pulisic","Estados Unidos","DEL","82","54","83","82","55"},
             {"Luka Jović","Serbia","DEL","78","50","80","74","20"},
             {"Samuel Chukwueze","Nigeria","DEL","80","46","82","84","35"},
             {"Marco Sportiello","Italia","POR","74","84","73","55","10"},
             {"Pierre Kalulu","Francia","DEF","68","82","80","80","30"}},

            {{"Gregor Kobel","Alemania","POR","80","87","78","62","60"},
             {"Mats Hummels","Alemania","DEF","62","86","82","70","35"},
             {"Nico Schlotterbeck","Alemania","DEF","66","83","81","74","50"},
             {"Niklas Süle","Alemania","DEF","65","84","82","72","45"},
             {"Raphaël Guerreiro","Portugal","DEF","72","78","82","82","35"},
             {"Julian Brandt","Alemania","MED","80","68","84","82","50"},
             {"Emre Can","Alemania","MED","74","76","83","76","25"},
             {"Marcel Sabitzer","Austria","MED","76","72","83","78","35"},
             {"Karim Adeyemi","Alemania","DEL","84","50","83","90","60"},
             {"Sébastien Haller","Costa de Marfil","DEL","80","52","82","78","25"},
             {"Felix Nmecha","Alemania","MED","76","70","82","78","30"},
             {"Giovanni Reyna","Estados Unidos","DEL","80","52","82","82","30"},
             {"Jamie Gittens","Inglaterra","DEL","80","48","82","88","30"},
             {"Donyell Malen","Países Bajos","DEL","82","48","82","88","40"},
             {"Alexander Meyer","Alemania","POR","74","84","73","55","8"}},

            {{"Remko Pasveer","Países Bajos","POR","76","85","75","56","15"},
             {"Devyne Rensch","Países Bajos","DEF","70","78","78","82","20"},
             {"Jorrel Hato","Países Bajos","DEF","66","80","79","78","20"},
             {"Josip Šutalo","Croacia","DEF","65","82","80","74","20"},
             {"Owen Wijndal","Países Bajos","DEF","68","76","79","80","15"},
             {"Steven Berghuis","Países Bajos","MED","78","68","82","78","30"},
             {"Jordan Henderson","Inglaterra","MED","72","72","80","72","15"},
             {"Kenneth Taylor","Países Bajos","MED","76","68","81","78","25"},
             {"Mohammed Kudus","Ghana","DEL","84","50","83","86","50"},
             {"Brian Brobbey","Países Bajos","DEL","82","52","83","80","35"},
             {"Dušan Tadić","Serbia","DEL","78","60","80","76","20"},
             {"Chuba Akpom","Nigeria","DEL","78","50","81","78","20"},
             {"Francisco Conceição","Portugal","DEL","80","50","82","84","30"},
             {"Jay Gorter","Países Bajos","POR","72","82","72","54","10"},
             {"Branco van den Boomen","Países Bajos","MED","74","68","80","76","15"}},

            {{"Diogo Costa","Portugal","POR","82","88","79","62","65"},
             {"João Mário","Portugal","DEF","72","80","81","82","35"},
             {"Pepe","Portugal","DEF","60","85","80","68","8"},
             {"David Carmo","Portugal","DEF","64","82","79","73","25"},
             {"Zaidu Sanusi","Nigeria","DEF","68","76","80","82","20"},
             {"Fábio Vieira","Portugal","MED","80","68","83","80","40"},
             {"Mateus Uribe","Colombia","MED","74","74","82","76","20"},
             {"Evanilson","Brasil","DEL","82","50","83","82","50"},
             {"Galeno","Brasil","DEL","82","50","83","86","45"},
             {"Mehdi Taremi","Irán","DEL","82","54","83","78","40"},
             {"Toni Martínez","España","DEL","78","50","80","76","15"},
             {"Pepê","Brasil","DEL","80","50","82","84","30"},
             {"Stephen Eustáquio","Canadá","MED","74","72","82","76","20"},
             {"Cláudio Ramos","Portugal","POR","74","83","73","55","8"},
             {"Iván Marcano","España","DEF","60","82","78","68","10"}},

            {{"Alex Meret","Italia","POR","80","87","78","60","45"},
             {"Giovanni Di Lorenzo","Italia","DEF","74","80","82","82","55"},
             {"Amir Rrahmani","Kosovo","DEF","65","83","81","73","40"},
             {"Min-jae Kim","Corea del Sur","DEF","67","84","83","76","60"},
             {"Mathías Olivera","Uruguay","DEF","70","78","80","82","35"},
             {"Piotr Zieliński","Polonia","MED","82","68","86","80","55"},
             {"André-Frank Zambo Anguissa","Camerún","MED","78","76","85","78","55"},
             {"Diego Demme","Alemania","MED","70","70","80","72","20"},
             {"Khvicha Kvaratskhelia","Georgia","DEL","88","50","86","88","100"},
             {"Victor Osimhen","Nigeria","DEL","90","52","87","88","120"},
             {"Giacomo Raspadori","Italia","DEL","80","56","82","78","45"},
             {"Matteo Politano","Italia","DEL","78","52","80","80","25"},
             {"Juan Jesus","Brasil","DEF","62","82","78","70","10"},
             {"Pierluigi Gollini","Italia","POR","74","83","73","55","10"},
             {"Elif Elmas","Macedonia","MED","76","68","81","78","25"}},

            {{"Odysseas Vlachodimos","Grecia","POR","80","86","77","60","40"},
             {"Alexander Bah","Dinamarca","DEF","70","78","80","82","30"},
             {"António Silva","Portugal","DEF","66","82","81","74","45"},
             {"Nicolás Otamendi","Argentina","DEF","62","84","80","70","25"},
             {"Grimaldo","España","DEF","74","78","82","84","50"},
             {"João Neves","Portugal","MED","80","72","84","80","70"},
             {"Fredrik Aursnes","Noruega","MED","74","73","82","78","30"},
             {"Florentino Luís","Portugal","MED","72","76","82","74","30"},
             {"Rafa Silva","Portugal","DEL","82","54","83","84","45"},
             {"Gonçalo Ramos","Portugal","DEL","82","52","84","80","65"},
             {"Petar Musa","Croacia","DEL","80","52","82","78","25"},
             {"Ángel Di María","Argentina","DEL","80","54","80","82","20"},
             {"David Neres","Brasil","DEL","82","50","82","86","40"},
             {"Henrique Araújo","Portugal","DEL","78","50","80","78","15"},
             {"Samuel Soares","Portugal","POR","72","82","72","54","8"}}
        };

        int jugadorId = 1;
        for (int i = 0; i < clubes.length; i++) {
            Object[] c = clubes[i];
            Equipo eq = new Equipo((String)c[0], (String)c[1], (double)c[2]);

            Entrenador ent = new Entrenador(
                1000 + i, (String)c[3], (int)c[4], "Internacional",
                (String)c[5], (int)c[6], (String)c[7]);
            eq.setEntrenador(ent);
            eq.setFormacion((String)c[7]);

            String[][] plts = plantillas[i];
            for (String[] jd : plts) {
                // jd: nombre, nacion, pos, ataque, defensa, energia, velocidad, valor
                Jugador j = new Jugador(
                    jugadorId++, jd[0], 20 + rnd.nextInt(15), jd[1], jd[2],
                    Integer.parseInt(jd[3]), Integer.parseInt(jd[4]),
                    Integer.parseInt(jd[5]), Integer.parseInt(jd[6]),
                    Double.parseDouble(jd[7]));
                eq.agregarJugador(j);
            }
            equipos.add(eq);
        }
        return equipos;
    }
}
