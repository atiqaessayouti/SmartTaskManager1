-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 12 fév. 2026 à 13:11
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
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `parent_id` int(11) DEFAULT NULL,
  `recurrence_type` varchar(50) DEFAULT 'NONE',
  `share_status` varchar(20) DEFAULT 'PENDING',
  `reminder_sent` tinyint(1) DEFAULT 0,
  `time_spent` bigint(20) DEFAULT 0,
  `timer_start` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `tasks`
--

INSERT INTO `tasks` (`id`, `title`, `description`, `priority`, `status`, `deadline`, `user_email`, `category`, `shared_with`, `created_at`, `parent_id`, `recurrence_type`, `share_status`, `reminder_sent`, `time_spent`, `timer_start`) VALUES
(1, 'Préparer Soutenance', 'Finir le PowerPoint et la démo', 'High', 'Completed', '2026-03-21', 'test@email.com', 'University', 'test@email.com', '2026-01-28 20:35:13', NULL, 'NONE', 'PENDING', 1, 93, NULL),
(3, 'USER', 'Vérifier les comptes inactifs', 'High', 'Completed', '2026-01-25', 'admin@email.com', 'Work', NULL, '2026-01-28 20:35:13', NULL, 'NONE', 'PENDING', 1, 0, '2026-02-12 11:00:53'),
(4, 'tache test', 'test', 'Low', 'In Progress', '2026-01-29', NULL, 'General', NULL, '2026-01-28 22:32:18', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(5, 'tache', 'h', 'Medium', 'In Progress', '2026-01-23', NULL, 'General', NULL, '2026-01-29 00:03:04', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(6, 'Révision examen ghda urgent', '', 'Medium', 'In Progress', NULL, NULL, 'General', NULL, '2026-01-29 01:59:51', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(7, 'Révision examen ghda urgent', '', 'Medium', 'In Progress', NULL, NULL, 'General', NULL, '2026-01-29 02:14:02', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(8, 'Révision examen ba3d ghda urgent', '', 'High', 'In Progress', '2026-01-30', NULL, 'General', NULL, '2026-01-29 02:16:11', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(11, 'Test Invitation', 'Jarbi wach notif khdama', 'High', 'Completed', '2026-03-07', 'admin@email.com', 'General', 'test@email.com', '2026-02-01 18:47:51', NULL, 'NONE', 'PENDING', 1, 165, NULL),
(12, 'Test Final Atiqa', 'Hadi khassha t-ban MARA WA7DA', 'High', 'In Progress', '2026-02-19', 'admin@email.com', 'General', 'test@email.com', '2026-02-01 19:04:29', NULL, 'NONE', 'PENDING', 1, 94, NULL),
(16, 'Examen Urgent', 'Khassni n-raja3 daba!', 'High', 'In Progress', '2026-01-10', 'test@email.com', 'General', NULL, '2026-02-01 19:56:42', NULL, 'NONE', 'PENDING', 1, 27, NULL),
(18, 'Sport', 'Jri chwiya', 'High', 'Completed', '2026-02-01', 'test@email.com', 'General', NULL, '2026-02-01 21:52:00', NULL, 'DAILY', 'PENDING', 0, 0, NULL),
(20, 'Sport', 'Jri chwiya', 'High', 'Completed', '2026-02-04', 'test@email.com', 'General', NULL, '2026-02-01 21:52:13', NULL, 'DAILY', 'PENDING', 0, 0, NULL),
(22, 'Sport', 'Jri chwiya', 'High', 'In Progress', '2026-02-05', 'test@email.com', 'General', NULL, '2026-02-01 21:52:18', NULL, 'DAILY', 'PENDING', 1, 0, NULL),
(27, 'Projet PFE', '', 'High', 'Completed', '2026-02-15', 'test@email.com', 'General', 'test@email.com', '2026-02-05 03:03:37', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(30, 'Réviser soutenance PFE demain urgent', '', 'High', 'In Progress', '2026-02-06', 'test@email.com', 'General', NULL, '2026-02-05 03:29:16', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(41, 'Finish report tomorrow urgent', '', 'High', 'In Progress', '2026-02-06', 'test@email.com', 'Work', NULL, '2026-02-05 04:09:03', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(46, 'Examen Java', 'Examen Java demain urgent', 'High', 'In Progress', '2026-02-07', 'test@email.com', 'Work', 'test@email.com', '2026-02-06 00:17:26', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(53, 'Payer facture internet', 'Payer facture internet chaque mois urgent GHDA', 'High', 'In Progress', '2026-02-07', 'test@email.com', 'Finance', NULL, '2026-02-06 01:22:39', NULL, 'MONTHLY', 'PENDING', 1, 0, NULL),
(54, 'HI', 'HI', 'Medium', 'In Progress', NULL, 'atiqaessayouti@gmail.com', 'General', NULL, '2026-02-06 03:46:24', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(55, 'test', 'test', 'Medium', 'In Progress', NULL, 'test@email.com', 'Education', NULL, '2026-02-06 13:47:41', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(56, 'test to day', 'test to day', 'Medium', 'In Progress', NULL, 'test@email.com', 'Education', NULL, '2026-02-06 13:48:39', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(58, 'TEST LYOU?', 'TEST URGENT LYOUM', 'High', 'In Progress', '2026-02-07', 'test@email.com', 'Education', NULL, '2026-02-07 00:35:31', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(59, 'EXAMEN', 'EXAMEN GHDA', 'High', 'In Progress', '2026-02-11', 'test@email.com', 'Education', 'test@email.com', '2026-02-10 21:11:14', NULL, 'NONE', 'ACCEPTED', 1, 0, NULL),
(60, 'Parent Task', 'Parent Task', 'Medium', 'Completed', '2026-02-08', 'test@email.com', 'General', NULL, '2026-02-10 22:43:31', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(61, 'Sub', 'Sub', 'Medium', 'Completed', NULL, 'test@email.com', 'General', NULL, '2026-02-10 22:44:16', 60, 'NONE', 'PENDING', 0, 0, NULL),
(62, 'Projet PFE', 'Projet PFE ', 'Medium', 'In Progress', NULL, 'test@email.com', 'Work', NULL, '2026-02-10 22:52:03', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(63, 'Chapitre 1', '', 'Medium', 'In Progress', '2026-02-08', 'test@email.com', 'General', NULL, '2026-02-10 22:52:17', 62, 'NONE', 'PENDING', 0, 0, NULL),
(64, 'TASK', 'TASK', 'Medium', 'Completed', NULL, 'test@email.com', 'General', NULL, '2026-02-10 23:02:01', NULL, 'WEEKLY', 'PENDING', 0, 0, NULL),
(65, 'TASK', 'TASK', 'Medium', 'Completed', '2026-02-18', 'test@email.com', 'General', 'test@email.com', '2026-02-10 23:02:06', NULL, 'WEEKLY', 'ACCEPTED', 1, 0, NULL),
(66, 'TASK', 'TASK', 'Medium', 'In Progress', '2026-02-25', 'test@email.com', 'General', NULL, '2026-02-10 23:02:09', NULL, 'WEEKLY', 'PENDING', 0, 0, NULL),
(67, 'TASK', 'TASK', 'Medium', 'In Progress', '2026-02-25', 'test@email.com', 'General', NULL, '2026-02-10 23:02:10', NULL, 'WEEKLY', 'PENDING', 0, 0, NULL),
(69, 'Review chapters 4 and 5', 'Review chapters 4 and 5 on Object Oriented Programming. Complete the practice test before the university tomorrow', 'Medium', 'Completed', '2026-02-12', 'ali@email.com', 'Education', 'testàemail.com', '2026-02-11 17:11:58', NULL, 'DAILY', 'PENDING', 0, 76, NULL),
(73, 'Complete preparation for the final presentation day.', 'Complete preparation for the final presentation day.', 'Medium', 'In Progress', '2026-02-18', 'ali@email.com', 'General', 'ali@email.com', '2026-02-11 17:20:19', NULL, 'NONE', 'ACCEPTED', 1, 0, NULL),
(74, 'Print 4 hard copies of the report for the jury members.', 'Print 4 hard copies of the report for the jury members.', 'Medium', 'Completed', '2026-02-12', 'ali@email.com', 'Work', NULL, '2026-02-11 17:22:50', 73, 'NONE', 'PENDING', 0, 30287, NULL),
(75, 'Test Partage Reel', 'Tache partagee pour le test', 'High', 'Completed', '2026-02-11', 'test@email.com', 'Test', 'ali@email.com', '2026-02-11 18:46:35', NULL, 'NONE', 'ACCEPTED', 1, 0, NULL),
(77, 'Meeting with the client morning urgen', 'Meeting with the client tomorrow morning urgen', 'Medium', 'In Progress', '2026-02-13', 'wissal@email.com', 'Work', NULL, '2026-02-12 01:57:24', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(80, 'Meeting with the client morning urgen', 'Meeting with the client tomorrow morning urgenT', 'High', 'Completed', '2026-02-06', 'ali@email.com', 'Work', 'test@email.com', '2026-02-12 02:05:56', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(81, 'Sub2', 'Sub2', 'Medium', 'Completed', '2026-02-07', 'ali@email.com', 'General', 'test@email.com', '2026-02-12 02:06:19', 80, 'NONE', 'ACCEPTED', 1, 0, NULL),
(82, 'Study JavaFX for my project', 'Study JavaFX for my project tomorrow urgent', 'High', 'Completed', '2026-02-08', 'admin@email.com', 'Work', 'test@email.com', '2026-02-12 10:00:11', NULL, 'NONE', 'PENDING', 1, 0, NULL),
(83, 'study', 'study', 'Medium', 'Completed', '2026-02-07', 'admin@email.com', 'Education', NULL, '2026-02-12 10:00:36', 82, 'NONE', 'PENDING', 0, 0, NULL),
(84, 'Task', 'Task', 'Medium', 'In Progress', '2026-02-14', 'ali@email.com', 'General', NULL, '2026-02-12 10:22:15', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(85, 'sub2', '', 'Medium', 'In Progress', NULL, 'ali@email.com', 'General', NULL, '2026-02-12 10:27:39', NULL, 'NONE', 'PENDING', 0, 0, NULL),
(86, 'subb', 'subb', 'Medium', 'Completed', '2026-02-05', 'ali@email.com', 'General', 'test@email.com', '2026-02-12 10:30:26', NULL, 'NONE', 'ACCEPTED', 1, 0, NULL);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `tasks`
--
ALTER TABLE `tasks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_email` (`user_email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `tasks`
--
ALTER TABLE `tasks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=87;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `tasks`
--
ALTER TABLE `tasks`
  ADD CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`user_email`) REFERENCES `users` (`email`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
