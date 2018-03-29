package de.adp.service.cds;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;

/**
 * Helper class for storing and delivering files from the local filesystem. 
 * @author simon.schwantzer(at)im-c.de
 */
public class LocalFileHandler {
	private final String contentDir;
	private final Logger logger;
	
	public LocalFileHandler(String contentDir, Logger logger) {
		this.contentDir = contentDir;
		this.logger = logger;
	}

	/**
	 * Resolves a request for a specific file of a content package.
	 * If the requested file does not exist, a 404 response will be created. 
	 * @param response HTTP server response to send file.
	 * @param contentId ID of the content package the file is located in.
	 * @param path Path of the file in the content package.
	 */
	public void resolveFileRequest(HttpServerResponse response, String contentId, String path) {
		StringBuilder builder = new StringBuilder(400);
		builder.append(contentDir);
		builder.append("/").append(contentId).append("/").append(path);
		response.sendFile(builder.toString());
	}
	
	/**
	 * Resolves a request for a content package by returning the related zip file.
	 * @param response HTTP server response to send content package. 
	 * @param contentId ID of the content package requested.
	 */
	public void resolveContentPackageRequest(HttpServerResponse response, String contentId) {
		StringBuilder builder = new StringBuilder(400);
		builder.append(contentDir);
		builder.append("/").append(contentId).append(".zip");
		response.sendFile(builder.toString());
	}
	
	/**
	 * Resolves a content package upload by storing and unpacking the uploaded zip file.
	 * Existing content packages with the same ID will be overwritten. 
	 * @param response HTTP server response to send process information.
	 * @param upload Upload to handle.
	 * @param contentId ID of the content package to be stored.
	 * @param redirectUrl URL to redirect if the process succeeds.
	 */
	public void resolveContentPackageUpload(final HttpServerResponse response, HttpServerFileUpload upload, final String contentId, final String redirectUrl) {
		StringBuilder builder = new StringBuilder(400);
		builder.append(contentDir);
		builder.append("/").append(contentId).append(".zip");
		final String zipFilePath = builder.toString();
		upload.streamToFileSystem(zipFilePath).endHandler(new Handler<Void>() {
			
			@Override
			public void handle(Void event) {
				StringBuilder builder = new StringBuilder(400);
				builder.append(contentDir).append("/").append(contentId);
				String targetDir = builder.toString();
				
				try {
					Path targetPath = Paths.get(targetDir);
					if (Files.exists(targetPath)) {
						removeRecursive(targetPath);
					}
					ZipUtil.unzip(zipFilePath, targetDir);
					response.headers().set("Location", redirectUrl);
					response.setStatusCode(303);
					response.end();
				} catch (IOException e) {
					response.setStatusCode(415);
					response.end("Failed to extract content: " + e.getMessage());
				}
			}
		});		
	}
	
	/**
	 * Resolve a request for the deletion of a content package.
	 * Both directory and related zip file will be removed.
	 * @param response Response to inform about progress.
	 * @param contentId ID of the content package to delete.
	 */
	public void resolveContentPackageDeletion(HttpServerResponse response, String contentId) {
		StringBuilder builder = new StringBuilder(400);
		builder.append(contentDir).append("/").append(contentId);
		String contentDir = builder.toString();
		Path contentDirPath = Paths.get(contentDir);
		builder.append(".zip");
		String contentZip = builder.toString();
		Path contentZipPath = Paths.get(contentZip);
		try {
			boolean exists = Files.deleteIfExists(contentZipPath);
			if (exists) {
				removeRecursive(contentDirPath);
				response.end();
			} else {
				response.setStatusCode(404);
				response.end("No content package with id " + contentId + " available.");
			}
		} catch (IOException e) {
			response.setStatusCode(500);
			response.end("Failed to delete content package: " + e.getMessage());
		}
	}
	
	/**
	 * Returns a list of all content packages locally available.
	 * @return List of content package IDs.
	 */
	public List<String> getLocalContentPackageList() {
		List<String> contentIds = new ArrayList<>();
		Path contentPath = Paths.get(contentDir);
		try {
			DirectoryStream<Path> dirStream = Files.newDirectoryStream(contentPath, new DirectoryStream.Filter<Path>() {

				@Override
				public boolean accept(Path entry) throws IOException {
					return Files.isDirectory(entry);
				}
			});
			for (Path path : dirStream) {
				contentIds.add(path.getFileName().toString());
			}
		} catch (IOException e) {
			logger.warn("Failed to retrieve content directory listing.", e);
		}
		return contentIds;
	}
	
	/**
	 * Recursively deletes an directory.
	 * @param path Path of the directory.
	 * @throws IOException Failed to delete directory.
	 */
	private static void removeRecursive(Path path) throws IOException {
		Files.walkFileTree(path, new FileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw new IOException("Failed to delete file: " + file.toString());
			}

	   });
	}
}
