-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 29 jan. 2026 à 04:08
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `smart_task_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `color_code` varchar(7) DEFAULT '#3498db'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categories`
--

INSERT INTO `categories` (`category_id`, `name`, `color_code`) VALUES
(1, 'University', '#e74c3c'),
(2, 'Work', '#f39c12'),
(3, 'Health', '#2ecc71');

-- --------------------------------------------------------

--
-- Structure de la table `comments`
--

CREATE TABLE `comments` (
  `id` int(11) NOT NULL,
  `task_id` int(11) NOT NULL,
  `user_email` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `attachment_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `comments`
--

INSERT INTO `comments` (`id`, `task_id`, `user_email`, `content`, `created_at`, `attachment_path`) VALUES
(1, 1, 'admin@email.com', 'Courage pour le projet!', '2026-01-28 20:35:13', NULL),
(2, 2, 'test@email.com', 'tache', '2026-01-29 00:01:23', NULL),
(3, 2, 'test@email.com', '', '2026-01-29 00:57:22', 'C:\\Users\\Octinz\\Documents\\attistation de ressite (1).pdf');

-- --------------------------------------------------------

--
-- Structure de la table `tasks`
--

CREATE TABLE `tasks` (
  `id` int(11) NOT NULL,
  `title` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `priority` varchar(20) DEFAULT 'Medium',
  `status` varchar(20) DEFAULT 'In Progress',
  `deadline` date DEFAULT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `category` varchar(50) DEFAULT 'General',
  `shared_with` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `tasks`
--

INSERT INTO `tasks` (`id`, `title`, `description`, `priority`, `status`, `deadline`, `user_email`, `category`, `shared_with`, `created_at`) VALUES
(1, 'Préparer Soutenance', 'Finir le PowerPoint et la démo', 'High', 'In Progress', '2026-01-08', 'test@email.com', 'University', NULL, '2026-01-28 20:35:13'),
(2, 'Sport', 'Courir 10km le matin', 'Low', 'Pending', '2026-01-17', 'test@email.com', 'Health', 'ali@email.com', '2026-01-28 20:35:13'),
(3, 'Audit Users', 'Vérifier les comptes inactifs', 'High', 'Completed', '2026-01-25', 'admin@email.com', 'Work', NULL, '2026-01-28 20:35:13'),
(4, 'tache test', 'test', 'Low', 'In Progress', '2026-01-29', NULL, 'General', NULL, '2026-01-28 22:32:18'),
(5, 'tache', 'h', 'Medium', 'In Progress', '2026-01-23', NULL, 'General', NULL, '2026-01-29 00:03:04'),
(6, 'Révision examen ghda urgent', '', 'Medium', 'In Progress', NULL, NULL, 'General', NULL, '2026-01-29 01:59:51'),
(7, 'Révision examen ghda urgent', '', 'Medium', 'In Progress', NULL, NULL, 'General', NULL, '2026-01-29 02:14:02'),
(8, 'Révision examen ba3d ghda urgent', '', 'High', 'In Progress', '2026-01-30', NULL, 'General', NULL, '2026-01-29 02:16:11');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(20) DEFAULT 'user',
  `bio` text DEFAULT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `password_hash`, `role`, `bio`, `image_path`, `created_at`) VALUES
(1, 'Admin', 'admin@email.com', '123456', 'admin', 'Super Admin System', NULL, '2026-01-28 20:35:12'),
(2, 'Etudiant', 'test@email.com', '123456', 'user', 'Master AI Student', NULL, '2026-01-28 20:35:12'),
(3, 'atiqa', 'atiqaessayouti@gmail.com', '123456', 'user', NULL, NULL, '2026-01-29 01:39:02');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`);

--
-- Index pour la table `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `task_id` (`task_id`);

--
-- Index pour la table `tasks`
--
ALTER TABLE `tasks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_email` (`user_email`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `comments`
--
ALTER TABLE `comments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `tasks`
--
ALTER TABLE `tasks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `tasks`
--
ALTER TABLE `tasks`
  ADD CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`user_email`) REFERENCES `users` (`email`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
