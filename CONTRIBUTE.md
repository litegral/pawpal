# 🐾 Panduan Kontribusi Proyek Pawpal

Panduan ini dibuat agar semua anggota tim bisa ikut kontribusi ke codebase Pawpal dengan alur yang jelas dan terstruktur. Alur ini adalah alur yang biasa dipakai di proyek open source, jadi bisa sekalian belajar cara kerja kolaboratif yang baik.

## ⚙️ Alur Kontribusi Utama

### 1. **Fork & Clone Repository**

Pertama-tama, pastikan sudah punya akun GitHub. Lakukan **fork** ke akun masing-masing, lalu **clone** repo hasil fork tersebut ke komputer lokal:

```bash
git clone https://github.com/username-anda/pawpal.git
cd pawpal
```

> Gantilah `username-anda` dengan nama akun GitHub kamu sendiri.

📘 Referensi: [GitHub Docs - Fork a repo](https://docs.github.com/en/get-started/quickstart/fork-a-repo)

---

### 2. **Tambahkan Remote Upstream**

Remote `upstream` dibutuhkan supaya kita bisa sinkronisasi dengan repository utama secara langsung.

```bash
git remote add upstream https://github.com/litegral/pawpal.git
```

> Gantilah `ORIGINAL_OWNER` dengan nama GitHub dari repo utama.

Untuk memastikan sudah ditambahkan:

```bash
git remote -v
```

📘 Referensi: [GitHub Docs - Configuring a remote for a fork](https://docs.github.com/en/get-started/quickstart/fork-a-repo#configure-git-to-sync-your-fork-with-the-original-repository)

---

### 3. **Buat Branch Baru**

Sebelum mulai ngoding, buat branch baru dari `main`. Ini penting supaya perubahan kita nggak langsung nyentuh code utama.

```bash
git checkout main
git pull upstream main
git checkout -b fitur/nama-fitur
```

Contoh:

```bash
git checkout -b fitur/jurnal-kucing
```

🧠 Tips: Gunakan penamaan yang deskriptif supaya branch-nya mudah dikenali.

---

### 4. **Kerjakan Perubahan di Branch Tersebut**

Mulai kerjakan task kamu di branch yang sudah dibuat. Usahakan satu branch hanya untuk satu fitur atau perbaikan biar gampang direview.

Kalau ada error atau test belum jalan, perbaiki dulu sebelum lanjut ke langkah berikutnya.

---

### 5. **Commit Perubahan Secara Teratur**

Setiap kali ada perubahan signifikan, lakukan commit. Gunakan pesan commit yang jelas dan singkat.

```bash
git add .
git commit -m "feat: tambah fitur jurnal perawatan kucing"
```

Prefix yang umum:

- `feat:` untuk fitur baru
- `fix:` untuk perbaikan bug
- `refactor:` untuk perbaikan struktur kode (gak ada perubahan fungsional dari aplikasi)
- `docs:` untuk dokumentasi

📘 Referensi: [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)

---

### 6. **Push Branch ke GitHub**

Kalau sudah selesai dan yakin tidak ada error:

```bash
git push origin fitur/nama-fitur
```

---

### 7. **Buat Pull Request (PR)**

Setelah push, buka GitHub lalu klik tombol **Compare & pull request**. Isi deskripsi PR dengan informasi yang jelas:

- Apa yang dikerjakan
- Catatan penting (kalau ada)

📘 Referensi: [GitHub Docs - Creating a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)

---

### 8. **Tunggu Review & Lakukan Revisi Jika Diperlukan**

Repo maintainer akan mereview PR kamu. Kalau ada masukan, perbaiki di branch yang sama lalu push ulang:

```bash
git add .
git commit -m "fix: perbaiki validasi input jurnal"
git push origin fitur/nama-fitur
```

---

### 9. **Merge ke Main (oleh PIC)**

Kalau PR sudah disetujui dan tidak ada konflik, maka branch akan di-merge ke `main` oleh maintainer.

---

## 🔄 Sinkronisasi Fork dengan Repository Utama

Sebelum memulai task baru atau membuat branch baru, pastikan `main` kamu selalu update dengan repo utama:

```bash
git checkout main
git fetch upstream
git merge upstream/main
```

Langkah ini membantu mencegah konflik saat kamu membuat pull request.

---

## ✅ Ringkasan Best Practices

- Selalu mulai dari branch baru, jangan langsung di `main`
- Fokus satu fitur per branch
- Pastikan kode sudah dicek sebelum push
- Gunakan pesan commit yang informatif
- Buat PR yang rapi dan jelas
- Selalu sync dengan upstream sebelum kerja

---

Kalau butuh bantuan lebih lanjut soal Git atau GitHub, bisa cek:

- [Git Handbook (GitHub)](https://guides.github.com/introduction/git-handbook/)
- [Git Cheatsheet PDF](https://education.github.com/git-cheat-sheet-education.pdf)
