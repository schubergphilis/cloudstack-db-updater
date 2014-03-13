package com.schubergphilis.cloudstackdb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.schubergphilis.utils.FileUtils;

public class SourceCodeVersion {

    private final Map<String, SourceCodeFile> files = new HashMap<String, SourceCodeFile>();
    private final File baseDir;

    public SourceCodeVersion(File baseDir) {
        this.baseDir = baseDir;
    }

    public void addFiles(Collection<File> files) {
        for (File file : files) {
            String fileAbsolutePath = file.getAbsolutePath();
            String baseDirAbsolutePath = baseDir.getAbsolutePath();
            if (fileDoesNotBelongToSourceCode(fileAbsolutePath, baseDirAbsolutePath)) {
                throw new RuntimeException(String.format("Trying to add a file that does not belong to this source code version.\nBase directory = '%s'\nFile = '%s'.",
                        baseDirAbsolutePath, fileAbsolutePath));
            }
            String pathRelativeToSourceRoot = removePrefix(fileAbsolutePath, baseDirAbsolutePath);
            this.files.put(pathRelativeToSourceRoot, new SourceCodeFile(file, pathRelativeToSourceRoot));
        }
    }

    public List<String> getFilesThatChangedInNewVersion(SourceCodeVersion newVersion) {
        List<String> filenames = new LinkedList<>();

        for (String filename : files.keySet()) {
            if (newVersion.containsFile(filename)) {
                try {
                    if (!FileUtils.filesHaveSameContentsNotConsideringTraillingWhiteSpace(files.get(filename).getFile(), newVersion.files.get(filename).getFile())) {
                        filenames.add(filename);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't compare the contents of file '" + filename + "' in previous and current version.", e);
                }
            }
        }

        return filenames;
    }

    public boolean containsFile(String filename) {
        return files.containsKey(filename);
    }

    public SourceCodeFile getFile(String filename) {
        return files.get(filename);
    }

    public Set<String> getAbsolutePaths() {
        return files.keySet();
    }

    public Collection<SourceCodeFile> getFiles() {
        return files.values();
    }

    protected Map<String, SourceCodeFile> getRelativePathToFilesMap() {
        return files;
    }

    protected static boolean fileDoesNotBelongToSourceCode(String fileAbsolutePath, String baseDirAbsolutePath) {
        return !fileAbsolutePath.startsWith(baseDirAbsolutePath);
    }

    protected static String removePrefix(String absolutePath, String prefix) {
        return absolutePath.replaceFirst(prefix, "");
    }

}
