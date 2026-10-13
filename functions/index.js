const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Scheduled Cloud Function ejecutada periódicamente (cada 1 hora) para purga automática 
 * de estados de 24 horas expirados (expiresAt <= Date.now()).
 * 
 * Acciones:
 * 1. Consultar estados expirados.
 * 2. Eliminar el archivo multimedia asociado en Firebase Storage / Bucket.
 * 3. Eliminar el documento de Firestore y sus subcolecciones asociadas (views).
 */
exports.purgeExpiredStatuses = onSchedule("every 1 hours", async (event) => {
  const db = admin.firestore();
  const bucket = admin.storage().bucket();
  const now = Date.now();

  console.log(`[Status Purge Cron] Iniciando purga automática a las ${new Date(now).toISOString()}`);

  try {
    // 1. Consultar únicamente estados con expiresAt <= Date.now()
    const expiredSnapshot = await db
      .collection("statuses")
      .where("expiresAt", "<=", now)
      .limit(200)
      .get();

    if (expiredSnapshot.empty) {
      console.log("[Status Purge Cron] No hay estados expirados pendientes.");
      return;
    }

    console.log(`[Status Purge Cron] Encontrados ${expiredSnapshot.size} estados expirados.`);

    let currentBatch = db.batch();
    let opCount = 0;

    for (const doc of expiredSnapshot.docs) {
      const data = doc.data();
      const statusId = doc.id;

      // 2. Eliminar archivo multimedia asociado en Firebase Storage si existe
      if (data.mediaUrl && data.mediaUrl.startsWith("http")) {
        try {
          const storagePath = data.storagePath || (data.userId ? `statuses/${data.userId}/${statusId}` : `statuses/${statusId}`);
          const fileRef = bucket.file(storagePath);
          await fileRef.delete({ ignoreNotFound: true });
          console.log(`[Status Purge Cron] Archivo en Storage eliminado (${storagePath}) para estado ${statusId}`);
        } catch (err) {
          console.warn(`[Status Purge Cron] Error al borrar storage para ${statusId}:`, err.message);
        }
      }

      // 3. Eliminar subcolección 'views' asociada
      const viewsSnapshot = await db.collection("statuses").doc(statusId).collection("views").get();
      for (const viewDoc of viewsSnapshot.docs) {
        currentBatch.delete(viewDoc.ref);
        opCount++;
        if (opCount >= 400) {
          await currentBatch.commit();
          currentBatch = db.batch();
          opCount = 0;
        }
      }

      // 4. Eliminar el documento de Firestore
      currentBatch.delete(doc.ref);
      opCount++;
      if (opCount >= 400) {
        await currentBatch.commit();
        currentBatch = db.batch();
        opCount = 0;
      }
    }

    if (opCount > 0) {
      await currentBatch.commit();
    }
    console.log("[Status Purge Cron] Purga completada exitosamente.");
  } catch (error) {
    console.error("[Status Purge Cron] Error en la ejecución de la función:", error);
  }
});
