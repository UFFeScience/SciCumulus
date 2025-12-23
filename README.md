# SciCumulus (Refactored)

> **A fully refactored, modernized, and maintainable version of the SciCumulus Scientific Workflow Management System**

---

## 📌 Overview

This repository contains a **complete architectural refactoring** of the original **SciCumulus** project developed at UFF.  
The refactoring preserves the **conceptual goals and execution semantics** of SciCumulus while radically improving:

- Maintainability
- Testability
- Modularity
- Extensibility
- Cloud and database portability

The result is a **clean-architecture implementation** suitable for **research, teaching, and production environments**.

---

## 🎯 Refactoring Goals

The refactor was driven by the following objectives:

- Eliminate tight coupling between workflow logic, database, and infrastructure
- Replace legacy and script-style code with modern Java architecture
- Enable unit and integration testing
- Support cloud and local execution transparently
- Provide clear documentation and architectural artifacts
- Prepare the codebase for long-term evolution

---

## 🧱 Architecture

The project follows **Clean Architecture** principles:

```
┌─────────────────────┐
│        API          │  CLI, user input
└─────────▲───────────┘
          │
┌─────────┴───────────┐
│        Core         │  Workflow, execution, scheduling
└─────────▲───────────┘
          │
┌─────────┴───────────┐
│   Infrastructure    │  Database, storage, filesystem
└─────────────────────┘
```

### Key Characteristics
- Core logic has **no dependency** on database, cloud, or filesystem
- Infrastructure components are **replaceable**
- All dependencies point **inward**

---

## 📂 Project Structure

```
src/
├── main/java/br/uff/scicumulus
│   ├── api/cli            # CLI entry point
│   ├── core               # Workflow engine (pure logic)
│   │   ├── workflow
│   │   ├── execution
│   │   ├── scheduling
│   │   └── provenance
│   └── infrastructure
│       ├── database
│       ├── storage
│       └── config
├── test/java              # Unit and integration tests
docs/
├── architecture.puml      # UML architecture diagram
├── refactoring-report.md  # Formal refactoring report
```

---

## 🧪 Testing

The project includes:

- **JUnit 5 unit tests**
- Testable core modules with no infrastructure dependencies
- In-memory provenance repository for safe integration testing

Run tests with:

```bash
mvn test
```

---

## 🗄️ Database & Provenance

- Provenance handling is abstracted via a repository interface
- Current implementation: `InMemoryProvenanceRepository`
- Easily extendable to PostgreSQL, MySQL, SQLite, or cloud databases

---

## ☁️ Cloud & Storage Support

Storage and execution are abstracted via interfaces:

- `StorageService`
- `LocalStorageService` (default)

This enables transparent integration with cloud providers and distributed filesystems.

---

## 📊 UML & Documentation

Included in `/docs`:

- **PlantUML architecture diagram**
- **Formal refactoring report** suitable for academic publication

---

## 🚀 Running the Application

### Prerequisites
- Java 11+
- Maven 3.8+

### Build
```bash
mvn clean package
```

### Run
```bash
java -cp target/scicumulus-2.0.0.jar br.uff.scicumulus.api.cli.Main
```

---

## 🔄 Migration from Original SciCumulus

This repository is **not a line-by-line rewrite**.  
It is a **structural refactoring** that preserves domain concepts while modernizing architecture.

---

## 📄 License

This project follows the **same license terms as the original SciCumulus project** unless otherwise specified.

---

## 🤝 Contributions

Contributions are welcome for:
- Database backends
- Cloud execution strategies
- Workflow parsers
- Performance evaluation

---

## 📬 Contact

For questions or collaboration, please open an issue.

---

✅ **Status:** Refactoring complete  
🧪 **Testable:** Yes  
☁️ **Cloud-ready:** Yes  
📚 **Publication-ready:** Yes
