package com.krlsedu.timetracker.desktop;

import com.krlsedu.timetracker.core.model.ApplicationDetail;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileVersion {
    public static void appDetails(ApplicationDetail applicationDetail) throws Exception {
        // http://msdn.microsoft.com/en-us/library/ms647464%28v=vs.85%29.aspx
        //
        // VerQueryValue will take two input and two output parameters
        // 1. parameter: is a pointer to the version-information returned
        //              by GetFileVersionInfo
        // 2. parameter: will take a string and return an output depending on
        //               the string:
        //     "\\"
        //         Is the root block and retrieves a VS_FIXEDFILEINFO struct
        //     "\\VarFileInfo\Translation"
        //         will return an array of Var variable information structure
        //         holding the language and code page identifier
        //     "\\StringFileInfo\\{lang-codepage}\\string-name"
        //         will return a string value of the language and code page
        //         requested. {lang-codepage} is a concatenation of a language
        //         and the codepage identifier pair found within the translation
        //         array in a hexadecimal string! string-name must be one of the
        //         following values:
        //             Comments, InternalName, ProductName, CompanyName,
        //             LegalCopyright, ProductVersion, FileDescription,
        //             LegalTrademarks, PrivateBuild, FileVersion,
        //             OriginalFilename, SpecialBuild
        // 3. parameter: contains the address of a pointer to the requested
        //               version information in the buffer of the 1st parameter.
        // 4. parameter: contains a pointer to the size of the requested data
        //               pointed to by the 3rd parameter. The length depends on
        //               the input of the 2nd parameter:
        //               *) For root block, the size in bytes of the structure
        //               *) For translation array values, the size in bytes of
        //                  the array stored at lplpBuffer;
        //               *) For version information values, the length in
        //                  character of the string stored at lplpBuffer;

        String filePath = applicationDetail.getProcessName();
        IntByReference dwDummy = new IntByReference();
        dwDummy.setValue(0);

        int versionlength =
                Version.INSTANCE.GetFileVersionInfoSize(filePath, dwDummy);

        if (versionlength > 0) {
            // will hold the bytes of the FileVersionInfo struct
            byte[] bufferarray = new byte[versionlength];
            // allocates space on the heap (== malloc in C/C++)
            Pointer lpData = new Memory(bufferarray.length);
            // will contain the address of a pointer to the requested version
            // information
            PointerByReference lplpBuffer = new PointerByReference();
            // will contain a pointer to the size of the requested data pointed
            // to by lplpBuffer.
            IntByReference puLen = new IntByReference();

            // reads versionLength bytes from the executable file into the FileVersionInfo struct buffer
            boolean fileInfoResult =
                    Version.INSTANCE.GetFileVersionInfo(
                            filePath, 0, versionlength, lpData);

            // retrieve file description for language and code page "i"
            boolean verQueryVal =
                    Version.INSTANCE.VerQueryValue(
                            lpData, "\\", lplpBuffer, puLen);

            // contains version information for a file. This information is
            // language and code page independent
            VS_FIXEDFILEINFO lplpBufStructure =
                    new VS_FIXEDFILEINFO(lplpBuffer.getValue());
            lplpBufStructure.read();

            int v1 = (lplpBufStructure.dwFileVersionMS).intValue() >> 16;
            int v2 = (lplpBufStructure.dwFileVersionMS).intValue() & 0xffff;
            int v3 = (lplpBufStructure.dwFileVersionLS).intValue() >> 16;
            int v4 = (lplpBufStructure.dwFileVersionLS).intValue() & 0xffff;

            String version =
                    v1 + "." +
                            v2 + "." +
                            v3 + "." +
                            v4;

            // creates a (reference) pointer
            PointerByReference lpTranslate = new PointerByReference();
            IntByReference cbTranslate = new IntByReference();
            // Read the list of languages and code pages
            verQueryVal = Version.INSTANCE.VerQueryValue(
                    lpData, "\\VarFileInfo\\Translation", lpTranslate, cbTranslate);

            if (cbTranslate.getValue() <= 0) {
                System.err.println("No translation found!");
                return;
            }

            // Read the file description
            // msdn has this example here:
            // for( i=0; i < (cbTranslate/sizeof(struct LANGANDCODEPAGE)); i++ )
            // where LANGANDCODEPAGE is a struct holding two WORDS. A word is
            // 16 bits (2x 8 bit = 2 bytes) long and as the struct contains two
            // words the length of the struct should be 4 bytes long
            for (int i = 0; i < (cbTranslate.getValue() / LANGANDCODEPAGE.sizeOf()); i++) {
                // writes formatted data to the specified string
                // out: pszDest - destination buffer which receives the formatted, null-terminated string created from pszFormat
                // in: ccDest - the size of the destination buffer, in characters. This value must be sufficiently large to accomodate the final formatted string plus 1 to account for the terminating null character.
                // in: pszFormat - the format string. This string must be null-terminated
                // in: ... The arguments to be inserted into the pszFormat string
                // hr = StringCchPrintf(SubBlock, 50,
                //                      TEXT("\\StringFileInfo\\%04x%04x\\FileDescription"),
                //                      lpTranslate[i].wLanguage,
                //                      lpTranslate[i].wCodePage);

                // fill the structure with the appropriate values
                LANGANDCODEPAGE langCodePage =
                        new LANGANDCODEPAGE(lpTranslate.getValue(), i * LANGANDCODEPAGE.sizeOf());
                langCodePage.read();

                // convert short values to hex-string:
                // https://stackoverflow.com/questions/923863/converting-a-string-to-hexadecimal-in-java
                String lang = String.format("%04x", langCodePage.wLanguage);
                String codePage = String.format("%04x", langCodePage.wCodePage);

                // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa381058.aspx
                // for proper values for lang and codePage

                LangAndCodePage.printTranslationInfo(lang.toUpperCase(), codePage.toUpperCase());

                // build the string for querying the file description stored in
                // the executable file
                StringBuilder subBlock = new StringBuilder();
                subBlock.append("\\StringFileInfo\\");
                subBlock.append(lang);
                subBlock.append(codePage);
                subBlock.append("\\FileDescription");

                try {
                    applicationDetail.setName(printDescription(lpData, subBlock.toString()));
                } catch (Exception e) {
                    applicationDetail.setName("");
                }
                applicationDetail.setAppVersion(version);
            }
        } else
            System.out.println("No version info available");

    }

    public static String printDescription(Pointer lpData, String subBlock) {
        PointerByReference lpBuffer = new PointerByReference();
        IntByReference dwBytes = new IntByReference();

        // Retrieve file description for language and code page "i"
        boolean verQueryVal = Version.INSTANCE.VerQueryValue(
                lpData, subBlock, lpBuffer, dwBytes);

        // a single character is represented by 2 bytes!
        // the last character is the terminating "\n"
        byte[] description =
                lpBuffer.getValue().getByteArray(0, (dwBytes.getValue() - 1) * 2);
        return new String(description, StandardCharsets.UTF_16LE);
    }

    // The structure as implemented by the MSDN article
    public static class LANGANDCODEPAGE extends Structure {
        /**
         * The language contained in the translation table
         **/
        public short wLanguage;
        /**
         * The code page contained in the translation table
         **/
        public short wCodePage;

        public LANGANDCODEPAGE(Pointer p) {
            useMemory(p);
        }

        public LANGANDCODEPAGE(Pointer p, int offset) {
            useMemory(p, offset);
        }

        public static int sizeOf() {
            return 4;
        }

        // newer versions of JNA require a field order to be set
        @Override
        protected List getFieldOrder() {
            List fieldOrder = new ArrayList();
            fieldOrder.add("wLanguage");
            fieldOrder.add("wCodePage");
            return fieldOrder;
        }
    }
}