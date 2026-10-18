package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PermitDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PermitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.ChecklistItem
import com.example.data.model.DigitalSignature
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PermitEntity::class, UserEntity::class, NotificationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun permitDao(): PermitDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "himcast_permit_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userDao = database.userDao()
            val permitDao = database.permitDao()
            val notifDao = database.notificationDao()
            val gson = Gson()

            val seedUsers = listOf(
                UserEntity(
                    name = "Amrish Parab (Admin)",
                    phone = "9359610323",
                    email = "Safety@himcast.in",
                    role = UserRole.ADMIN,
                    department = "Safety & EHS Management"
                ),
                UserEntity(
                    name = "Rajesh Patil",
                    phone = "9822012345",
                    email = "r.patil@himcast.in",
                    role = UserRole.SUPERVISOR,
                    department = "Foundry Maintenance"
                ),
                UserEntity(
                    name = "Vikas Deshmukh",
                    phone = "9822023456",
                    email = "v.deshmukh@himcast.in",
                    role = UserRole.SAFETY_OFFICER,
                    department = "Plant Safety"
                ),
                UserEntity(
                    name = "Suresh Kulkarni",
                    phone = "9822034567",
                    email = "s.kulkarni@himcast.in",
                    role = UserRole.DEPT_HEAD,
                    department = "Melting & Casting Dept"
                ),
                UserEntity(
                    name = "Mahesh Shinde",
                    phone = "9822045678",
                    email = "m.shinde@himcast.in",
                    role = UserRole.PLANT_HEAD,
                    department = "Works & Plant Operations"
                ),
                UserEntity(
                    name = "Contractor - Shivam Engg",
                    phone = "9822099999",
                    email = "shivam.engg@gmail.com",
                    role = UserRole.CONTRACTOR,
                    department = "Mechanical Maintenance Agency"
                )
            )
            userDao.insertUsers(seedUsers)

            val now = System.currentTimeMillis()
            val oneHour = 3600 * 1000L

            // Standard checklist
            val hotWorkChecklist = listOf(
                ChecklistItem("1", "Surrounding combustible materials cleared (min 10m radius)", true),
                ChecklistItem("2", "Fire extinguishers (CO2 + DCP) inspected and kept ready", true),
                ChecklistItem("3", "Gas cylinders kept upright, chained, with flash back arrestors", true),
                ChecklistItem("4", "PPE (leather apron, face shield, welding goggles, safety boots) issued", true),
                ChecklistItem("5", "Trained fire watcher assigned at location", true)
            )

            val heightWorkChecklist = listOf(
                ChecklistItem("1", "Full body safety harness inspected with double lanyard", true),
                ChecklistItem("2", "Scaffolding tagged GREEN (inspected by competent engineer)", true),
                ChecklistItem("3", "Lifeline securely anchored to rigid overhead structural member", true),
                ChecklistItem("4", "Hard hat with chin strap and safety boots worn", true),
                ChecklistItem("5", "Barricading and caution boards placed at ground level", true)
            )

            val confinedSpaceChecklist = listOf(
                ChecklistItem("1", "Atmospheric gas testing done (O2 between 19.5% and 23.5%)", true),
                ChecklistItem("2", "Positive forced air ventilation blower operational", true),
                ChecklistItem("3", "24V safety handlamp and intrinsically safe torch provided", true),
                ChecklistItem("4", "Dedicated standby person stationed at manhole entry", true),
                ChecklistItem("5", "LOTO isolation verified on all incoming pipes and electrical feeds", true)
            )

            val seedPermits = listOf(
                // 1. APPROVED Permit
                PermitEntity(
                    permitNumber = "HC-2026-001",
                    category = PermitCategory.HOT_WORK,
                    title = "Induction Furnace Lining Welding",
                    description = "Arc welding and repair on refractory coil support beam of Furnace #3.",
                    location = "Furnace Bay C, Ground Floor",
                    department = "Melting Operations",
                    contractorName = "Shivam Engineering",
                    contractorSupervisor = "Ganesh Jadhav",
                    contractorPhone = "9822099999",
                    numberOfWorkers = 4,
                    issuerName = "Rajesh Patil",
                    issuerPhone = "9822012345",
                    status = PermitStatus.APPROVED,
                    startTime = now - (2 * oneHour),
                    endTime = now + (6 * oneHour), // Valid for 6 more hours
                    createdAt = now - (3 * oneHour),
                    checklistJson = gson.toJson(hotWorkChecklist),
                    signaturesJson = gson.toJson(listOf(
                        DigitalSignature("Site Supervisor (Issuer)", "Rajesh Patil", "9822012345", "SIG_ISSUER_DATA", now - (3 * oneHour), "Job area verified"),
                        DigitalSignature("Department Head", "Suresh Kulkarni", "9822034567", "SIG_DEPT_DATA", now - (2 * oneHour), "Approved for Furnace Bay C"),
                        DigitalSignature("Safety Officer", "Vikas Deshmukh", "9822023456", "SIG_SAFETY_DATA", now - (2 * oneHour), "LOTO & Fire watcher confirmed"),
                        DigitalSignature("Contractor Representative", "Ganesh Jadhav", "9822099999", "SIG_CONTRACTOR_DATA", now - (2 * oneHour), "Workers briefed on emergency SOP")
                    )),
                    isClosed = false
                ),
                // 2. PENDING Permit
                PermitEntity(
                    permitNumber = "HC-2026-002",
                    category = PermitCategory.HEIGHT_WORK,
                    title = "Overhead Crane Rail Track Inspection & Repair",
                    description = "Structural inspection and alignment of 15T EOT Crane runway rail at 8.5 meters.",
                    location = "Foundry Shed 2, Overhead Bay",
                    department = "Mechanical Maintenance",
                    contractorName = "Apex Industrial Services",
                    contractorSupervisor = "Sunil More",
                    contractorPhone = "9823155555",
                    numberOfWorkers = 3,
                    issuerName = "Rajesh Patil",
                    issuerPhone = "9822012345",
                    status = PermitStatus.PENDING,
                    startTime = now + (1 * oneHour),
                    endTime = now + (9 * oneHour),
                    createdAt = now - (30 * 60 * 1000L),
                    checklistJson = gson.toJson(heightWorkChecklist),
                    signaturesJson = gson.toJson(listOf(
                        DigitalSignature("Site Supervisor (Issuer)", "Rajesh Patil", "9822012345", "SIG_ISSUER_DATA", now - (30 * 60 * 1000L), "Permit drafted and issued")
                    )),
                    isClosed = false
                ),
                // 3. EXPIRED Permit
                PermitEntity(
                    permitNumber = "HC-2026-003",
                    category = PermitCategory.CONFINED_SPACE,
                    title = "Sand Reclamation Pit Cleaning & Sump Pump Inspection",
                    description = "Entry into 3.5m deep return sand slurry pit for residue cleanout and pump unblocking.",
                    location = "Moulding Section, Pit Area #4",
                    department = "Moulding & Core Shop",
                    contractorName = "Dynamic Heavy Equipments",
                    contractorSupervisor = "Prakash Pawar",
                    contractorPhone = "9822188888",
                    numberOfWorkers = 2,
                    issuerName = "Rajesh Patil",
                    issuerPhone = "9822012345",
                    status = PermitStatus.EXPIRED,
                    startTime = now - (14 * oneHour),
                    endTime = now - (2 * oneHour), // Expired 2 hours ago
                    createdAt = now - (16 * oneHour),
                    checklistJson = gson.toJson(confinedSpaceChecklist),
                    signaturesJson = gson.toJson(listOf(
                        DigitalSignature("Site Supervisor (Issuer)", "Rajesh Patil", "9822012345", "SIG_ISSUER_DATA", now - (16 * oneHour), "Issued"),
                        DigitalSignature("Department Head", "Suresh Kulkarni", "9822034567", "SIG_DEPT_DATA", now - (15 * oneHour), "Approved"),
                        DigitalSignature("Safety Officer", "Vikas Deshmukh", "9822023456", "SIG_SAFETY_DATA", now - (15 * oneHour), "Gas test cleared"),
                        DigitalSignature("Contractor Representative", "Prakash Pawar", "9822188888", "SIG_CONTRACTOR_DATA", now - (15 * oneHour), "Accepted")
                    )),
                    isClosed = false
                ),
                // 4. APPROVED Permit
                PermitEntity(
                    permitNumber = "HC-2026-004",
                    category = PermitCategory.ELECTRICAL,
                    title = "HT Substation Transformer Oil Filtration",
                    description = "11kV / 433V Transformer T-2 bushing check and dialectric oil filtration.",
                    location = "Main Substation Yard, Zone 1",
                    department = "Electrical Maintenance",
                    contractorName = "Techno Power Solutions",
                    contractorSupervisor = "Anil Shinde",
                    contractorPhone = "9822077777",
                    numberOfWorkers = 3,
                    issuerName = "Rajesh Patil",
                    issuerPhone = "9822012345",
                    status = PermitStatus.APPROVED,
                    startTime = now - (1 * oneHour),
                    endTime = now + (5 * oneHour),
                    createdAt = now - (2 * oneHour),
                    checklistJson = gson.toJson(hotWorkChecklist),
                    signaturesJson = gson.toJson(listOf(
                        DigitalSignature("Site Supervisor (Issuer)", "Rajesh Patil", "9822012345", "SIG_ISSUER_DATA", now - (2 * oneHour), "Issued"),
                        DigitalSignature("Safety Officer", "Vikas Deshmukh", "9822023456", "SIG_SAFETY_DATA", now - (1 * oneHour), "Isolation tag locked")
                    )),
                    isClosed = false
                )
            )

            seedPermits.forEach { permitDao.insertPermit(it) }

            notifDao.insertNotification(
                NotificationEntity(
                    permitId = 1L,
                    permitNumber = "HC-2026-001",
                    title = "Permit Approved",
                    message = "Hot work permit HC-2026-001 has been fully authorized by Safety Officer Vikas Deshmukh.",
                    emailRecipientRole = "Site Supervisor"
                )
            )
            notifDao.insertNotification(
                NotificationEntity(
                    permitId = 2L,
                    permitNumber = "HC-2026-002",
                    title = "Pending Approval Required",
                    message = "Height work permit HC-2026-002 requires Department Head & Safety Officer signatures.",
                    emailRecipientRole = "Department Head"
                )
            )
            notifDao.insertNotification(
                NotificationEntity(
                    permitId = 3L,
                    permitNumber = "HC-2026-003",
                    title = "Permit Expired Alert",
                    message = "Confined space permit HC-2026-003 expired at Pit Area #4. Stop all work or request extension.",
                    emailRecipientRole = "Safety Officer"
                )
            )
        }
    }
}
