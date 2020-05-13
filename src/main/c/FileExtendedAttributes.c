//
// Created by Stevo on 5/7/20.
//

#include <jni.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <sys/stat.h>
#include <sys/xattr.h>
#include <unistd.h>

//----------------------------------------------------------------------------------------------------------------------
jstring Java_codes_stevobrock_androidtoolbox_model_FileExtendedAttributes_get(JNIEnv* env, jclass clazz, jstring pathString, jstring nameString) {
	// Setup
	const	char*	path = (*env)->GetStringUTFChars(env, pathString, NULL);
	const	char*	name = (*env)->GetStringUTFChars(env, nameString, NULL);

			jstring	valueString = NULL;

	// Query size
	ssize_t size = getxattr(path, name, NULL, 0);
	if (size > 0) {
		// Read string
		char value[size + 1];
		ssize_t result = getxattr(path, name, value, (size_t) size);
		value[size] = 0;

		// Check result
		if (result >= 0)
			// Success
			valueString = (*env)->NewStringUTF(env, (const char*) value);
		else {
			// Error
			jclass exClass = (*env)->FindClass(env, "java/io/IOException");
			(*env)->ThrowNew(env, exClass, strerror(errno));
		}
	} else {
		// Try sidecar file
		size_t	pathLength = strrchr(path, '/') - path + 1;
		char	extendedAttributePath[(*env)->GetStringUTFLength(env, pathString) + (*env)->GetStringUTFLength(env, nameString) + 21];
		strncpy(extendedAttributePath, path, pathLength);
		sprintf(extendedAttributePath + pathLength, ".%s-ExtendedAttribute-%s", path + pathLength, name);

		// Get size
		struct stat	st;
		if (stat(extendedAttributePath, &st) == 0) {
			// Open
			int	fd = open(extendedAttributePath, O_RDONLY);
			if (fd != -1) {
				char	value[st.st_size + 1];
				int 	result = read(fd, value, (size_t) st.st_size);
				if (result > 0) {
					// Success
					value[st.st_size] = 0;
					valueString = (*env)->NewStringUTF(env, (const char*) value);
				}

				// Cleanup
				close(fd);
			}
		}
	}

	// Cleanup
	(*env)->ReleaseStringUTFChars(env, pathString, path);
	(*env)->ReleaseStringUTFChars(env, nameString, name);

	return valueString;
}

//----------------------------------------------------------------------------------------------------------------------
void Java_codes_stevobrock_androidtoolbox_model_FileExtendedAttributes_set(JNIEnv* env, jclass clazz, jstring pathString, jstring nameString, jstring valueString) {
	// Setup
	const	char*	path = (*env)->GetStringUTFChars(env, pathString, NULL);
	const	char*	name = (*env)->GetStringUTFChars(env, nameString, NULL);
	const	char*	value = (*env)->GetStringUTFChars(env, valueString, NULL);

	// Set extended attribute
	int count = setxattr(path, name, value, strlen(value), 0);
	if ((count == -1) && (errno == 95)) {
		// Extended attributes are not supported on this filesystem, use sidecar file
		size_t	pathLength = strrchr(path, '/') - path + 1;
		char	extendedAttributePath[(*env)->GetStringUTFLength(env, pathString) + (*env)->GetStringUTFLength(env, nameString) + 21];
		strncpy(extendedAttributePath, path, pathLength);
		sprintf(extendedAttributePath + pathLength, ".%s-ExtendedAttribute-%s", path + pathLength, name);

		// Write
		int	fd = open(extendedAttributePath, O_WRONLY | O_CREAT | O_TRUNC, S_IRUSR | S_IWUSR);
		count = write(fd, value, strlen(value));
		close(fd);

		// Check result
		if (count == -1) {
			// Error
			jclass exClass = (*env)->FindClass(env, "java/io/IOException");
			(*env)->ThrowNew(env, exClass, strerror(errno));
		}
	}

	// Cleanup
	(*env)->ReleaseStringUTFChars(env, pathString, path);
	(*env)->ReleaseStringUTFChars(env, nameString, name);
	(*env)->ReleaseStringUTFChars(env, valueString, value);
}
