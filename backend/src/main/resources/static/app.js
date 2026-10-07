// Panel web del inventario.
// Habla con la API REST del mismo servidor (rutas /api/...), así que no hace falta configurar CORS.

const URL_API = "/api";

// Campos del formulario de equipo (coinciden con los atributos de la clase Equipo del backend)
const CAMPOS_EQUIPO = ["nombre", "tipo", "marca", "modelo", "numeroSerie", "ubicacion", "estado"];

const ETIQUETAS_ESTADO = {
    OPERATIVO: "Operativo",
    AVERIADO: "Averiado",
    EN_REPARACION: "En reparación",
    BAJA: "Baja",
};

// Estado de la pantalla
let codigoEnEdicion = null;   // null = se está creando un equipo nuevo
let equipoAbierto = null;     // equipo que se muestra en la ficha
let temporizadorBusqueda = null;

// Elementos de la página
const buscador = document.getElementById("buscador");
const resumen = document.getElementById("resumen");
const cuerpoTabla = document.getElementById("cuerpoTabla");
const mensajeVacio = document.getElementById("vacio");
const dialogoFormulario = document.getElementById("dialogoFormulario");
const formulario = document.getElementById("formulario");
const dialogoDetalle = document.getElementById("dialogoDetalle");
const formularioIncidencia = document.getElementById("formularioIncidencia");
const textoIncidencia = document.getElementById("textoIncidencia");

// ---------- Utilidades ----------

// Envuelve fetch: añade la cabecera JSON y convierte los errores del servidor en excepciones
async function llamarApi(ruta, opciones = {}) {
    const respuesta = await fetch(URL_API + ruta, {
        headers: { "Content-Type": "application/json" },
        ...opciones,
    });
    if (!respuesta.ok) {
        const error = await respuesta.json().catch(() => ({}));
        throw new Error(error.message || "Error " + respuesta.status);
    }
    return respuesta.status === 204 ? null : respuesta.json();
}

// Evita que un texto con etiquetas HTML se interprete como código al pintarlo en la página
function escaparHtml(texto) {
    const contenedor = document.createElement("div");
    contenedor.textContent = texto ?? "";
    return contenedor.innerHTML;
}

function crearEtiquetaEstado(estado) {
    return `<span class="estado estado-${estado}">${ETIQUETAS_ESTADO[estado] ?? estado}</span>`;
}

function formatearFecha(fechaIso) {
    return new Date(fechaIso).toLocaleString("es-ES", { dateStyle: "short", timeStyle: "short" });
}

// ---------- Tabla de equipos ----------

async function cargarEquipos() {
    try {
        const texto = encodeURIComponent(buscador.value.trim());
        const equipos = await llamarApi("/equipos?texto=" + texto);
        pintarTabla(equipos);
    } catch (error) {
        alert("No se pudieron cargar los equipos: " + error.message);
    }
}

function pintarTabla(equipos) {
    cuerpoTabla.innerHTML = "";

    equipos.forEach((equipo) => {
        const fila = document.createElement("tr");
        fila.tabIndex = 0;
        fila.innerHTML = `
            <td class="codigo">${escaparHtml(equipo.codigoNfc)}</td>
            <td>${escaparHtml(equipo.nombre)}</td>
            <td>${escaparHtml(equipo.tipo)}</td>
            <td>${escaparHtml(equipo.ubicacion)}</td>
            <td>${crearEtiquetaEstado(equipo.estado)}</td>`;
        fila.addEventListener("click", () => abrirDetalle(equipo.codigoNfc));
        fila.addEventListener("keydown", (evento) => {
            if (evento.key === "Enter") abrirDetalle(equipo.codigoNfc);
        });
        cuerpoTabla.appendChild(fila);
    });

    const averiados = equipos.filter((equipo) => equipo.estado === "AVERIADO").length;
    resumen.textContent = `${equipos.length} equipos, ${averiados} averiados`;
    mensajeVacio.hidden = equipos.length > 0;
}

// Espera un momento tras la última tecla para no llamar a la API en cada letra
buscador.addEventListener("input", () => {
    clearTimeout(temporizadorBusqueda);
    temporizadorBusqueda = setTimeout(cargarEquipos, 250);
});

// ---------- Crear y editar ----------

function abrirFormulario(equipo = null) {
    codigoEnEdicion = equipo ? equipo.codigoNfc : null;
    document.getElementById("tituloFormulario").textContent = equipo ? "Editar equipo" : "Nuevo equipo";

    CAMPOS_EQUIPO.forEach((campo) => {
        formulario.elements[campo].value = equipo?.[campo] ?? (campo === "estado" ? "OPERATIVO" : "");
    });
    dialogoFormulario.showModal();
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const datos = {};
    CAMPOS_EQUIPO.forEach((campo) => {
        datos[campo] = formulario.elements[campo].value.trim();
    });

    try {
        const guardado = codigoEnEdicion
            ? await llamarApi("/equipos/" + codigoEnEdicion, { method: "PUT", body: JSON.stringify(datos) })
            : await llamarApi("/equipos", { method: "POST", body: JSON.stringify(datos) });

        dialogoFormulario.close();
        await cargarEquipos();
        await abrirDetalle(guardado.codigoNfc);
    } catch (error) {
        alert("No se pudo guardar el equipo: " + error.message);
    }
});

document.getElementById("botonNuevo").addEventListener("click", () => abrirFormulario());
document.getElementById("botonCancelar").addEventListener("click", () => dialogoFormulario.close());

// ---------- Ficha del equipo ----------

async function abrirDetalle(codigo) {
    try {
        const [equipo, incidencias] = await Promise.all([
            llamarApi("/equipos/" + codigo),
            llamarApi(`/equipos/${codigo}/incidencias`),
        ]);
        equipoAbierto = equipo;
        pintarDetalle(equipo);
        pintarIncidencias(incidencias);
        if (!dialogoDetalle.open) dialogoDetalle.showModal();
    } catch (error) {
        alert("No se pudo abrir el equipo: " + error.message);
    }
}

function pintarDetalle(equipo) {
    document.getElementById("tituloDetalle").textContent = equipo.nombre;

    const filas = [
        ["Código NFC", equipo.codigoNfc],
        ["Estado", crearEtiquetaEstado(equipo.estado)],
        ["Tipo", escaparHtml(equipo.tipo)],
        ["Marca", escaparHtml(equipo.marca)],
        ["Modelo", escaparHtml(equipo.modelo)],
        ["Número de serie", escaparHtml(equipo.numeroSerie)],
        ["Ubicación", escaparHtml(equipo.ubicacion)],
        ["Fecha de alta", formatearFecha(equipo.fechaAlta)],
    ];
    document.getElementById("datosEquipo").innerHTML = filas
        .map(([etiqueta, valor]) => `<dt>${etiqueta}</dt><dd>${valor || "-"}</dd>`)
        .join("");
}

function pintarIncidencias(incidencias) {
    const lista = document.getElementById("listaIncidencias");
    lista.innerHTML = "";

    if (incidencias.length === 0) {
        lista.innerHTML = "<li>Este equipo no tiene incidencias.</li>";
        return;
    }

    incidencias.forEach((incidencia) => {
        const elemento = document.createElement("li");
        elemento.innerHTML = `
            <div class="${incidencia.resuelta ? "resuelta" : ""}">
                ${escaparHtml(incidencia.descripcion)}
                <small>${formatearFecha(incidencia.fecha)}</small>
            </div>`;

        if (!incidencia.resuelta) {
            const boton = document.createElement("button");
            boton.type = "button";
            boton.className = "boton";
            boton.textContent = "Marcar como resuelta";
            boton.addEventListener("click", () => resolverIncidencia(incidencia.id));
            elemento.appendChild(boton);
        }
        lista.appendChild(elemento);
    });
}

formularioIncidencia.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    try {
        await llamarApi(`/equipos/${equipoAbierto.codigoNfc}/incidencias`, {
            method: "POST",
            body: JSON.stringify({ descripcion: textoIncidencia.value.trim() }),
        });
        textoIncidencia.value = "";
        await abrirDetalle(equipoAbierto.codigoNfc);
        await cargarEquipos();
    } catch (error) {
        alert("No se pudo reportar la incidencia: " + error.message);
    }
});

async function resolverIncidencia(id) {
    try {
        await llamarApi(`/incidencias/${id}/resolver`, { method: "PUT" });
        await abrirDetalle(equipoAbierto.codigoNfc);
        await cargarEquipos();
    } catch (error) {
        alert("No se pudo resolver la incidencia: " + error.message);
    }
}

document.getElementById("botonEditar").addEventListener("click", () => {
    dialogoDetalle.close();
    abrirFormulario(equipoAbierto);
});

document.getElementById("botonEliminar").addEventListener("click", async () => {
    const mensaje = `¿Eliminar "${equipoAbierto.nombre}"? También se borrará su historial de incidencias.`;
    if (!confirm(mensaje)) return;

    try {
        await llamarApi("/equipos/" + equipoAbierto.codigoNfc, { method: "DELETE" });
        dialogoDetalle.close();
        await cargarEquipos();
    } catch (error) {
        alert("No se pudo eliminar el equipo: " + error.message);
    }
});

document.getElementById("botonCerrar").addEventListener("click", () => dialogoDetalle.close());

// Al abrir la página se carga la lista completa
cargarEquipos();
