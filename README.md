# EventForge - Smart Event Planning & Backup Management System

## Run
    javac *.java
    java Main
(Choose option 7 to load a ready-made demo event.)

## Where each OOP concept is used
| Concept | Where |
|---|---|
| Classes & Objects | Event, Allocation, Venue, Vendor, Volunteer, Equipment |
| Encapsulation | Private fields + validated getters/setters in Resource, Event |
| Abstraction | abstract class Resource with abstract getType() |
| Inheritance | Venue, Vendor, Volunteer, Equipment extend Resource |
| Polymorphism | Overridden describe()/getCost(); Resource references hold any subclass |
| Interfaces | Backupable, Reportable (implemented by Allocation / Event) |
| Exception handling | BudgetExceededException, InvalidBackupException, NoBackupException, try/catch in Main |
| Collections | ArrayList (events, allocations), TreeMap (cost breakdown by type) |
| Enum | EventStatus |
| Static | Resource.counter (auto-ID), Allocation.RESERVE_RATE |

## Backup logic
Every primary resource gets a backup of the same type. A 10% standby fee is reserved
from the budget. If the primary fails (option 5), activating the backup (option 6)
swaps it in and charges the backup's full cost instead.
