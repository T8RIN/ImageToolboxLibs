# Changelog

## [v2.4.0](https://github.com/uclouvain/openjpeg/releases/v2.4.0) (2020-12-28)

[Full Changelog](https://github.com/uclouvain/openjpeg/compare/v2.3.1...v2.4.0)

**Closed issues:**

- OPENJPEG\_INSTALL\_DOC\_DIR does not control a destination directory where HTML docs would be
  installed. [\#1309](https://github.com/uclouvain/openjpeg/issues/1309)
- Heap-buffer-overflow in lib/openjp2/pi.c:
  312 [\#1302](https://github.com/uclouvain/openjpeg/issues/1302)
- Heap-buffer-overflow in lib/openjp2/t2.c:
  973 [\#1299](https://github.com/uclouvain/openjpeg/issues/1299)
- Heap-buffer-overflow in lib/openjp2/pi.c:
  623 [\#1293](https://github.com/uclouvain/openjpeg/issues/1293)
- Global-buffer-overflow in lib/openjp2/dwt.c:
  1980 [\#1286](https://github.com/uclouvain/openjpeg/issues/1286)
- Heap-buffer-overflow in lib/openjp2/tcd.c:
  2417 [\#1284](https://github.com/uclouvain/openjpeg/issues/1284)
- Heap-buffer-overflow in lib/openjp2/mqc.c:
  499 [\#1283](https://github.com/uclouvain/openjpeg/issues/1283)
- Openjpeg could not encode 32bit RGB float
  image [\#1281](https://github.com/uclouvain/openjpeg/issues/1281)
- Openjpeg could not encode 32bit RGB float
  image [\#1280](https://github.com/uclouvain/openjpeg/issues/1280)
- ISO/IEC 15444-1:2019 \(E\) compared with '
  cio.h' [\#1277](https://github.com/uclouvain/openjpeg/issues/1277)
- Test-suite failure due to hash
  mismatch [\#1264](https://github.com/uclouvain/openjpeg/issues/1264)
- Heap use-after-free [\#1261](https://github.com/uclouvain/openjpeg/issues/1261)
- Memory leak when failing to allocate
  object... [\#1259](https://github.com/uclouvain/openjpeg/issues/1259)
- Memory leak of Tier 1 handle when OpenJPEG fails to set it as
  TLS... [\#1257](https://github.com/uclouvain/openjpeg/issues/1257)
- Any plan to build release for
  CVE-2020-8112/CVE-2020-6851 [\#1247](https://github.com/uclouvain/openjpeg/issues/1247)
- failing to convert 16-bit file: opj\_t2\_encode\_packet\(\): only 5251 bytes remaining in output
  buffer. 5621 needed. [\#1243](https://github.com/uclouvain/openjpeg/issues/1243)
- CMake+VS2017 Compile OK, thirdparty Compile OK, but thirdparty not
  install [\#1239](https://github.com/uclouvain/openjpeg/issues/1239)
- New release to solve CVE-2019-6988 ? [\#1238](https://github.com/uclouvain/openjpeg/issues/1238)
- Many tests fail to pass after the update of libtiff to version
  4.1.0 [\#1233](https://github.com/uclouvain/openjpeg/issues/1233)
- Another heap buffer overflow in
  libopenjp2 [\#1231](https://github.com/uclouvain/openjpeg/issues/1231)
- Heap buffer overflow in libopenjp2 [\#1228](https://github.com/uclouvain/openjpeg/issues/1228)
- Endianness of binary volume \(JP3D\) [\#1224](https://github.com/uclouvain/openjpeg/issues/1224)
- New release to resolve CVE-2019-12973 [\#1222](https://github.com/uclouvain/openjpeg/issues/1222)
- how to set the block size,like
  128,256 ? [\#1216](https://github.com/uclouvain/openjpeg/issues/1216)
- compress YUV files to motion jpeg2000
  standard [\#1213](https://github.com/uclouvain/openjpeg/issues/1213)
- Repair/update Java wrapper, and include in
  release [\#1208](https://github.com/uclouvain/openjpeg/issues/1208)
- abc [\#1206](https://github.com/uclouvain/openjpeg/issues/1206)
- Slow decoding [\#1202](https://github.com/uclouvain/openjpeg/issues/1202)
- Installation question [\#1201](https://github.com/uclouvain/openjpeg/issues/1201)
- Typo in test\_decode\_area - \*ptilew is assigned instead of
  \*ptileh [\#1195](https://github.com/uclouvain/openjpeg/issues/1195)
- Creating a J2K file with one POC is
  broken [\#1191](https://github.com/uclouvain/openjpeg/issues/1191)
- Make fails on Arch Linux [\#1174](https://github.com/uclouvain/openjpeg/issues/1174)
- Heap buffer overflow in opj\_t1\_clbl\_decode\_processor\(\) triggered with
  Ghostscript [\#1158](https://github.com/uclouvain/openjpeg/issues/1158)
- opj\_stream\_get\_number\_byte\_left: Assertion `p\_stream-\>m\_byte\_offset \>= 0'
  failed. [\#1151](https://github.com/uclouvain/openjpeg/issues/1151)
- The fuzzer ignores too many inputs [\#1079](https://github.com/uclouvain/openjpeg/issues/1079)
- out of bounds read [\#1068](https://github.com/uclouvain/openjpeg/issues/1068)

**Merged pull requests:**

- Change defined
  WIN32 [\#1310](https://github.com/uclouvain/openjpeg/pull/1310) ([Jamaika1](https://github.com/Jamaika1))
- docs: fix simple typo, producted -\>
  produced [\#1308](https://github.com/uclouvain/openjpeg/pull/1308) ([timgates42](https://github.com/timgates42))
- Set ${OPENJPEG\_INSTALL\_DOC\_DIR} to DESTINATION of
  HTMLs [\#1307](https://github.com/uclouvain/openjpeg/pull/1307) ([lemniscati](https://github.com/lemniscati))
- Use INC\_DIR for OPENJPEG\_INCLUDE\_DIRS \(fixes
  uclouvain\#1174\) [\#1306](https://github.com/uclouvain/openjpeg/pull/1306) ([matthew-sharp](https://github.com/matthew-sharp))
- pi.c: avoid out of bounds access with POC \(fixes
  \#1302\) [\#1304](https://github.com/uclouvain/openjpeg/pull/1304) ([rouault](https://github.com/rouault))
- Encoder: grow again buffer
  size [\#1303](https://github.com/uclouvain/openjpeg/pull/1303) ([zodf0055980](https://github.com/zodf0055980))
- opj\_j2k\_write\_sod\(\): avoid potential heap buffer overflow \(fixes \#1299\) \(probably master
  only\) [\#1301](https://github.com/uclouvain/openjpeg/pull/1301) ([rouault](https://github.com/rouault))
- pi.c: avoid out of bounds access with POC
  \(refs https://github.com/uclouvain/openjpeg/issues/1293\#issuecomment-737122836\) [\#1300](https://github.com/uclouvain/openjpeg/pull/1300) ([rouault](https://github.com/rouault))
- opj\_t2\_encode\_packet\(\): avoid out of bound access of \#1297, but likely not the proper
  fix [\#1298](https://github.com/uclouvain/openjpeg/pull/1298) ([rouault](https://github.com/rouault))
- opj\_t2\_encode\_packet\(\): avoid out of bound access of \#1294, but likely not the proper
  fix [\#1296](https://github.com/uclouvain/openjpeg/pull/1296) ([rouault](https://github.com/rouault))
- opj\_j2k\_setup\_encoder\(\): validate POC compno0 and compno1 \(fixes
  \#1293\) [\#1295](https://github.com/uclouvain/openjpeg/pull/1295) ([rouault](https://github.com/rouault))
- Encoder: avoid global buffer overflow on irreversible conversion
  when… [\#1292](https://github.com/uclouvain/openjpeg/pull/1292) ([rouault](https://github.com/rouault))
- Decoding: deal with some SPOT6 images that have tiles with a single tile-part with TPsot == 0 and
  TNsot == 0, and with missing
  EOC [\#1291](https://github.com/uclouvain/openjpeg/pull/1291) ([rouault](https://github.com/rouault))
- Free p\_tcd\_marker\_info to avoid memory
  leak [\#1288](https://github.com/uclouvain/openjpeg/pull/1288) ([zodf0055980](https://github.com/zodf0055980))
- Encoder: grow again buffer
  size [\#1287](https://github.com/uclouvain/openjpeg/pull/1287) ([zodf0055980](https://github.com/zodf0055980))
- Encoder: avoid uint32 overflow when allocating memory for codestream buffer \(fixes
  \#1243\) [\#1276](https://github.com/uclouvain/openjpeg/pull/1276) ([rouault](https://github.com/rouault))
- Java compatibility from 1.5 to
  1.6 [\#1263](https://github.com/uclouvain/openjpeg/pull/1263) ([jiapei100](https://github.com/jiapei100))
- opj\_decompress: fix double-free on input directory with mix of valid and invalid
  images [\#1262](https://github.com/uclouvain/openjpeg/pull/1262) ([rouault](https://github.com/rouault))
- openjp2: Plug image leak when failing to allocate codestream
  index. [\#1260](https://github.com/uclouvain/openjpeg/pull/1260) ([sebras](https://github.com/sebras))
- openjp2: Plug memory leak when setting data as TLS
  fails. [\#1258](https://github.com/uclouvain/openjpeg/pull/1258) ([sebras](https://github.com/sebras))
- openjp2: Error out if failing to create Tier 1
  handle. [\#1256](https://github.com/uclouvain/openjpeg/pull/1256) ([sebras](https://github.com/sebras))
- Testing for invalid values of width, height,
  numcomps [\#1254](https://github.com/uclouvain/openjpeg/pull/1254) ([szukw000](https://github.com/szukw000))
- Single-threaded performance improvements in forward DWT for 5-3 and 9-7 \(and other
  improvements\) [\#1253](https://github.com/uclouvain/openjpeg/pull/1253) ([rouault](https://github.com/rouault))
- Add support for multithreading in
  encoder [\#1248](https://github.com/uclouvain/openjpeg/pull/1248) ([rouault](https://github.com/rouault))
- Add support for generation of PLT markers in
  encoder [\#1246](https://github.com/uclouvain/openjpeg/pull/1246) ([rouault](https://github.com/rouault))
- Fix warnings about signed/unsigned casts in
  pi.c [\#1244](https://github.com/uclouvain/openjpeg/pull/1244) ([rouault](https://github.com/rouault))
- opj\_decompress: add sanity checks to avoid segfault in case of decoding
  error [\#1240](https://github.com/uclouvain/openjpeg/pull/1240) ([rouault](https://github.com/rouault))
- ignore wrong
  icc [\#1236](https://github.com/uclouvain/openjpeg/pull/1236) ([szukw000](https://github.com/szukw000))
- Implement writing of IMF
  profiles [\#1235](https://github.com/uclouvain/openjpeg/pull/1235) ([rouault](https://github.com/rouault))
- tests: add alternate checksums for libtiff
  4.1 [\#1234](https://github.com/uclouvain/openjpeg/pull/1234) ([rouault](https://github.com/rouault))
- opj\_tcd\_init\_tile\(\): avoid integer
  overflow [\#1232](https://github.com/uclouvain/openjpeg/pull/1232) ([rouault](https://github.com/rouault))
- tests/fuzzers: link fuzz binaries using
  $LIB\_FUZZING\_ENGINE. [\#1230](https://github.com/uclouvain/openjpeg/pull/1230) ([Dor1s](https://github.com/Dor1s))
- opj\_j2k\_update\_image\_dimensions\(\): reject images whose coordinates are beyond INT\_MAX
  \(fixes
  \#1228\) [\#1229](https://github.com/uclouvain/openjpeg/pull/1229) ([rouault](https://github.com/rouault))
- Fix resource
  leaks [\#1226](https://github.com/uclouvain/openjpeg/pull/1226) ([dodys](https://github.com/dodys))
- abi-check.sh: fix false postive ABI error, and display output error
  log [\#1218](https://github.com/uclouvain/openjpeg/pull/1218) ([rouault](https://github.com/rouault))
- pi.c: avoid integer overflow, resulting in later invalid access to memory in
  opj\_t2\_decode\_packets\(\) [\#1217](https://github.com/uclouvain/openjpeg/pull/1217) ([rouault](https://github.com/rouault))
- Add check to validate SGcod/SPcoc/SPcod parameter
  values. [\#1211](https://github.com/uclouvain/openjpeg/pull/1211) ([sebras](https://github.com/sebras))
- Fix buffer overflow reading an image file less than four
  characters [\#1196](https://github.com/uclouvain/openjpeg/pull/1196) ([robert-ancell](https://github.com/robert-ancell))
- compression: emit POC marker when only one single POC is requested
  \(f… [\#1192](https://github.com/uclouvain/openjpeg/pull/1192) ([rouault](https://github.com/rouault))
- Fix several potential
  vulnerabilities  [\#1185](https://github.com/uclouvain/openjpeg/pull/1185) ([Young-X](https://github.com/Young-X))
- openjp2/j2k: Report error if all wanted components are not
  decoded. [\#1164](https://github.com/uclouvain/openjpeg/pull/1164) ([sebras](https://github.com/sebras))

## [v2.3.1](https://github.com/uclouvain/openjpeg/releases/v2.3.1) (2019-04-02)

[Full Changelog](https://github.com/uclouvain/openjpeg/compare/v2.3.0...v2.3.1)

**Closed issues:**

- v2.2.0 regression for decoding images where TNsot ==
  0 [\#1120](https://github.com/uclouvain/openjpeg/issues/1120)
- Int overflow in jp3d [\#1162](https://github.com/uclouvain/openjpeg/issues/1162)
- Heap buffer overflow in opj\_j2k\_update\_image\_data\(\) triggered with
  Ghostscript [\#1157](https://github.com/uclouvain/openjpeg/issues/1157)
- LINUX install doesn't work when building shared libraries is
  disabled [\#1155](https://github.com/uclouvain/openjpeg/issues/1155)
- OPENJPEG null ptr dereference in openjpeg-2.3.0/src/bin/jp2/convert.c:
  2243 [\#1152](https://github.com/uclouvain/openjpeg/issues/1152)
- How to drop certain subbands/layers in
  DWT [\#1147](https://github.com/uclouvain/openjpeg/issues/1147)
- where is the MQ-Coder ouput stream in
  t2.c? [\#1146](https://github.com/uclouvain/openjpeg/issues/1146)
- OpenJPEG 2.3 \(and 2.2?\) multi component image fails to decode with KDU
  v7.10 [\#1132](https://github.com/uclouvain/openjpeg/issues/1132)
- Missing checks for header\_info.height and header\_info.width in function pnmtoimage in
  src/bin/jpwl/convert.c, which can lead to heap buffer
  overflow [\#1126](https://github.com/uclouvain/openjpeg/issues/1126)
- Assertion Failure in jp2.c [\#1125](https://github.com/uclouvain/openjpeg/issues/1125)
- Division-by-zero vulnerabilities in the function pi\_next\_pcrl, pi\_next\_cprl and pi\_next\_rpcl
  in src/lib/openjp3d/pi.c [\#1123](https://github.com/uclouvain/openjpeg/issues/1123)
- Precinct switch \(-c\) doesn't right-shift last record to remaining resolution
  levels [\#1117](https://github.com/uclouvain/openjpeg/issues/1117)
- Sample: encode J2K a data using
  streams??? [\#1114](https://github.com/uclouvain/openjpeg/issues/1114)
- HIGH THROUGHPUT JPEG 2000 \(HTJ2K\) [\#1112](https://github.com/uclouvain/openjpeg/issues/1112)
- How to build openjpeg for arm linux? [\#1108](https://github.com/uclouvain/openjpeg/issues/1108)
- crash [\#1106](https://github.com/uclouvain/openjpeg/issues/1106)
- JP2000 returning OPJ\_CLRSPC\_UNKNOWN color
  space [\#1103](https://github.com/uclouvain/openjpeg/issues/1103)
- Compilation successful but install unsuccessful: Calling executables throws libraries missing
  error [\#1102](https://github.com/uclouvain/openjpeg/issues/1102)
- fprintf format string requires 1 parameter but only 0 are
  given [\#1093](https://github.com/uclouvain/openjpeg/issues/1093)
- fprintf format string requires 1 parameter but only 0 are
  given [\#1092](https://github.com/uclouvain/openjpeg/issues/1092)
- sprintf buffer overflow [\#1088](https://github.com/uclouvain/openjpeg/issues/1088)
- sprintf buffer overflow [\#1085](https://github.com/uclouvain/openjpeg/issues/1085)
- Infinite loop when reading jp2 [\#1081](https://github.com/uclouvain/openjpeg/issues/1081)
- missing format string parameter [\#1074](https://github.com/uclouvain/openjpeg/issues/1074)
- Excessive Iteration in opj\_t1\_encode\_cblks
  \(src/lib/openjp2/t1.c\) [\#1059](https://github.com/uclouvain/openjpeg/issues/1059)
- Out-of-bound left shift in opj\_j2k\_setup\_encoder
  \(src/lib/openjp2/j2k.c\) [\#1057](https://github.com/uclouvain/openjpeg/issues/1057)
- Encode image on Unsplash [\#1054](https://github.com/uclouvain/openjpeg/issues/1054)
- Integer overflow in opj\_t1\_encode\_cblks
  \(src/lib/openjp2/t1.c\) [\#1053](https://github.com/uclouvain/openjpeg/issues/1053)
- Signed Integer Overflow - 68065512 [\#1048](https://github.com/uclouvain/openjpeg/issues/1048)
- Similar vulnerable functions related to
  CVE-2017-14041 [\#1044](https://github.com/uclouvain/openjpeg/issues/1044)
- \[ERROR\] COD marker already read. No more than one COD marker per
  tile.  [\#1043](https://github.com/uclouvain/openjpeg/issues/1043)
- failing to install latest version of openjpeg from
  source [\#1041](https://github.com/uclouvain/openjpeg/issues/1041)
- Trouble compressing large raw image [\#1032](https://github.com/uclouvain/openjpeg/issues/1032)
- Download and installed code from 2.3 archive. Installing
  2.2? [\#1030](https://github.com/uclouvain/openjpeg/issues/1030)
- missing fclose [\#1029](https://github.com/uclouvain/openjpeg/issues/1029)
- NULL Pointer Access in function imagetopnm of convert.c\(jp2\):
  1289 [\#860](https://github.com/uclouvain/openjpeg/issues/860)
- NULL Pointer Access in function imagetopnm of convert.c:
  2226\(jp2\)  [\#859](https://github.com/uclouvain/openjpeg/issues/859)
- Heap Buffer Overflow in function imagetotga of convert.c\(jp2\):
  942 [\#858](https://github.com/uclouvain/openjpeg/issues/858)

**Merged pull requests:**

- abi-check.sh: fix broken download
  URL [\#1188](https://github.com/uclouvain/openjpeg/pull/1188) ([rouault](https://github.com/rouault))
- opj\_t1\_encode\_cblks: fix UBSAN signed integer
  overflow [\#1187](https://github.com/uclouvain/openjpeg/pull/1187) ([rouault](https://github.com/rouault))
- convertbmp: detect invalid file dimensions early
  \(CVE-2018-6616\) [\#1172](https://github.com/uclouvain/openjpeg/pull/1172) ([hlef](https://github.com/hlef))
- color\_apply\_icc\_profile: avoid potential heap buffer
  overflow [\#1170](https://github.com/uclouvain/openjpeg/pull/1170) ([rouault](https://github.com/rouault))
- Fix multiple potential vulnerabilities and
  bugs [\#1168](https://github.com/uclouvain/openjpeg/pull/1168) ([Young-X](https://github.com/Young-X))
- Fix several memory and resource
  leaks [\#1163](https://github.com/uclouvain/openjpeg/pull/1163) ([nforro](https://github.com/nforro))
- Fix some potential overflow
  issues [\#1161](https://github.com/uclouvain/openjpeg/pull/1161) ([stweil](https://github.com/stweil))
- jp3d/jpwl convert: fix write stack buffer
  overflow [\#1160](https://github.com/uclouvain/openjpeg/pull/1160) ([hlef](https://github.com/hlef))
- Int overflow
  fixed [\#1159](https://github.com/uclouvain/openjpeg/pull/1159) ([ichlubna](https://github.com/ichlubna))
- Update knownfailures- files given current
  configurations [\#1149](https://github.com/uclouvain/openjpeg/pull/1149) ([rouault](https://github.com/rouault))
- CVE-2018-5785: fix issues with zero
  bitmasks [\#1148](https://github.com/uclouvain/openjpeg/pull/1148) ([hlef](https://github.com/hlef))
- openjp2/jp2: Fix two format
  strings [\#1143](https://github.com/uclouvain/openjpeg/pull/1143) ([stweil](https://github.com/stweil))
- Changes in pnmtoimage if image data are
  missing [\#1141](https://github.com/uclouvain/openjpeg/pull/1141) ([szukw000](https://github.com/szukw000))
- Relative path to header files is hardcoded in OpenJPEGConfig.cmake.in
  file [\#1140](https://github.com/uclouvain/openjpeg/pull/1140) ([bukatlib](https://github.com/bukatlib))
- Cast on uint
  ceildiv [\#1136](https://github.com/uclouvain/openjpeg/pull/1136) ([reverson](https://github.com/reverson))
- Add -DBUILD\_PKGCONFIG\_FILES to install
  instructions [\#1133](https://github.com/uclouvain/openjpeg/pull/1133) ([robe2](https://github.com/robe2))
- Fix some typos in code comments and
  documentation [\#1128](https://github.com/uclouvain/openjpeg/pull/1128) ([stweil](https://github.com/stweil))
- Fix regression in reading files with TNsot == 0 \(refs
  \#1120\) [\#1121](https://github.com/uclouvain/openjpeg/pull/1121) ([rouault](https://github.com/rouault))
- Use local type declaration for POSIX standard type only for MS
  compiler [\#1119](https://github.com/uclouvain/openjpeg/pull/1119) ([stweil](https://github.com/stweil))
- Fix Mac
  builds [\#1104](https://github.com/uclouvain/openjpeg/pull/1104) ([rouault](https://github.com/rouault))
- jp3d: Replace sprintf\(\) by snprintf\(\) in
  volumetobin\(\) [\#1101](https://github.com/uclouvain/openjpeg/pull/1101) ([kbabioch](https://github.com/kbabioch))
- opj\_mj2\_extract: Rename output\_location to
  output\_prefix [\#1096](https://github.com/uclouvain/openjpeg/pull/1096) ([kbabioch](https://github.com/kbabioch))
- mj2: Add missing variable to format string in fprintf\(\) invocation in
  meta\_out.c [\#1094](https://github.com/uclouvain/openjpeg/pull/1094) ([kbabioch](https://github.com/kbabioch))
- Convert files to UTF-8
  encoding [\#1090](https://github.com/uclouvain/openjpeg/pull/1090) ([stweil](https://github.com/stweil))
- fix unchecked integer multiplication
  overflow [\#1080](https://github.com/uclouvain/openjpeg/pull/1080) ([setharnold](https://github.com/setharnold))
- Fixed
  typos [\#1062](https://github.com/uclouvain/openjpeg/pull/1062) ([radarhere](https://github.com/radarhere))
- Note that seek uses SEEK\_SET
  behavior. [\#1055](https://github.com/uclouvain/openjpeg/pull/1055) ([ideasman42](https://github.com/ideasman42))
- Some Doxygen tags are
  removed [\#1050](https://github.com/uclouvain/openjpeg/pull/1050) ([szukw000](https://github.com/szukw000))
- Fix resource leak \(CID
  179466\) [\#1047](https://github.com/uclouvain/openjpeg/pull/1047) ([stweil](https://github.com/stweil))
- Changed cmake version test to allow for cmake
  2.8.11.x [\#1042](https://github.com/uclouvain/openjpeg/pull/1042) ([radarhere](https://github.com/radarhere))
- Add missing fclose\(\) statement in error
  condition. [\#1037](https://github.com/uclouvain/openjpeg/pull/1037) ([gfiumara](https://github.com/gfiumara))

## [v2.3.0](https://github.com/uclouvain/openjpeg/releases/v2.3.0) (2017-10-04)

[Full Changelog](https://github.com/uclouvain/openjpeg/compare/v2.2.0...v2.3.0)

**Implemented enhancements:**

- Sub-tile decoding: only decode precincts and codeblocks that intersect the window specified in
  opj_set_decode_area() [\#990](https://github.com/uclouvain/openjpeg/pull/990) ([rouault](https://github.com/rouault))
- Sub-tile decoding: only apply IDWT on areas that participate to the window of
  interest [\#1001](https://github.com/uclouvain/openjpeg/pull/1001) ([rouault](https://github.com/rouault))
- Sub-tile decoding: memory use reduction and perf
  improvements [\#1010](https://github.com/uclouvain/openjpeg/pull/1010) ([rouault](https://github.com/rouault))
- Add capability to decode only a subset of all components of an
  image. [\#1022](https://github.com/uclouvain/openjpeg/pull/1022) ([rouault](https://github.com/rouault))

**Fixed bugs:**

- Setting x offset of decode region to -1 causes opj\_decompress to go into infinite
  loop [\#736](https://github.com/uclouvain/openjpeg/issues/736)
- Problem decoding multiple tiles with get\_decoded\_tile when cmap/pclr/cdef boxes are present in
  jp2 file [\#484](https://github.com/uclouvain/openjpeg/issues/484)
- set reduce\_factor\_may\_fail [\#474](https://github.com/uclouvain/openjpeg/issues/474)
- opj\_compress.exe, command line parser, infinite
  loop [\#469](https://github.com/uclouvain/openjpeg/issues/469)
- Various memory access issues found via
  fuzzing [\#448](https://github.com/uclouvain/openjpeg/issues/448)
- Multiple warnings when building OpenJPEG
  \(trunk\) [\#442](https://github.com/uclouvain/openjpeg/issues/442)
- Bulk fuzz-testing report [\#427](https://github.com/uclouvain/openjpeg/issues/427)
- remove all printf from openjpeg / use proper function pointer for
  logging [\#371](https://github.com/uclouvain/openjpeg/issues/371)
- minor changes, clean-up [\#349](https://github.com/uclouvain/openjpeg/issues/349)
- image-\>numcomps \> 4 [\#333](https://github.com/uclouvain/openjpeg/issues/333)
- Improve support for region of interest [\#39](https://github.com/uclouvain/openjpeg/issues/39)
- Public function to tell kernel type used \(5x3 vs
  9x7\) [\#3](https://github.com/uclouvain/openjpeg/issues/3)
- elf binary in source package ?  [\#1026](https://github.com/uclouvain/openjpeg/issues/1026)
- opj\_cio\_open [\#1025](https://github.com/uclouvain/openjpeg/issues/1025)
- Building with Visual Studio 2015 [\#1023](https://github.com/uclouvain/openjpeg/issues/1023)
- tcd.cpp\>:1617:33: error: assigning to 'OPJ\_INT32 \*' \(aka 'int \*'\) from incompatible type '
  void \*' [\#1021](https://github.com/uclouvain/openjpeg/issues/1021)
- j2k.cpp \> comparison of address of 'p\_j2k-\>m\_cp.tcps\[0\].m\_data' not equal to a null pointer
  is always true [\#1020](https://github.com/uclouvain/openjpeg/issues/1020)
- Openjpeg 2.2.0 always build shared library even though -DBUILD\_SHARED\_LIBS:
  bool=off [\#1019](https://github.com/uclouvain/openjpeg/issues/1019)
- missing fclose [\#1018](https://github.com/uclouvain/openjpeg/issues/1018)
- Use opj\_image\_data\_free instead of opj\_free for
  image-\>comps\[\].data [\#1014](https://github.com/uclouvain/openjpeg/issues/1014)
- malloc poison on some compilers - cross
  compiling [\#1013](https://github.com/uclouvain/openjpeg/issues/1013)
- Add OPJ\_VERSION\_MAJOR, OPJ\_VERSION\_MINOR, OPJ\_VERSION\_MICRO macros in
  openjpeg.h [\#1011](https://github.com/uclouvain/openjpeg/issues/1011)
- Encode: do not perform rate control for single-tile
  lossless [\#1009](https://github.com/uclouvain/openjpeg/issues/1009)
- opj\_set\_decoded\_resolution\_factor\(\): bad interaction with opj\_set\_decode\_area\(\) and/or
  opj\_decode\(\) [\#1006](https://github.com/uclouvain/openjpeg/issues/1006)
- memory allocation failure with .pgx file [\#999](https://github.com/uclouvain/openjpeg/issues/999)
- Unable to fuzz with raw image as input [\#998](https://github.com/uclouvain/openjpeg/issues/998)
- stack-based buffer overflow write in pgxtoimage
  \(/convert.c\) [\#997](https://github.com/uclouvain/openjpeg/issues/997)
- freeze with a crafted bmp [\#996](https://github.com/uclouvain/openjpeg/issues/996)
- invalid memory write in tgatoimage
  \(convert.c\) [\#995](https://github.com/uclouvain/openjpeg/issues/995)
- static build on Windows fails [\#994](https://github.com/uclouvain/openjpeg/issues/994)
- another heap-based buffer overflow in opj\_t2\_encode\_packet
  \(t2.c\) [\#993](https://github.com/uclouvain/openjpeg/issues/993)
- heap-based buffer overflow in opj\_t2\_encode\_packet
  \(t2.c\) [\#992](https://github.com/uclouvain/openjpeg/issues/992)
- heap-based buffer overflow in opj\_write\_bytes\_LE \(cio.c\) \(unfixed
  \#985\) [\#991](https://github.com/uclouvain/openjpeg/issues/991)
- heap overflow in opj\_compress [\#988](https://github.com/uclouvain/openjpeg/issues/988)
- heap overflow in opj\_decompress [\#987](https://github.com/uclouvain/openjpeg/issues/987)
- heap-based buffer overflow in opj\_bio\_byteout
  \(bio.c\) [\#986](https://github.com/uclouvain/openjpeg/issues/986)
- heap-based buffer overflow in opj\_write\_bytes\_LE
  \(cio.c\) [\#985](https://github.com/uclouvain/openjpeg/issues/985)
- memory allocation failure in opj\_aligned\_alloc\_n
  \(opj\_malloc.c\) [\#983](https://github.com/uclouvain/openjpeg/issues/983)
- heap-base buffer overflow in opj\_mqc\_flush
  \(mqc.c\) [\#982](https://github.com/uclouvain/openjpeg/issues/982)
- Decode fails for JP2s with ICC profile [\#981](https://github.com/uclouvain/openjpeg/issues/981)
- Unit tests failing on Ubuntu 17.04 [\#916](https://github.com/uclouvain/openjpeg/issues/916)
- Encoder crashes on small images [\#901](https://github.com/uclouvain/openjpeg/issues/901)
- openjpeg-1.5.3 fails to compile [\#830](https://github.com/uclouvain/openjpeg/issues/830)
- opj\_compress crops image \(win\) or creates a jp2 which cannot be decompressed
  \(lin\) [\#716](https://github.com/uclouvain/openjpeg/issues/716)
- -d flag is silently ignored when decoding a single
  tile [\#693](https://github.com/uclouvain/openjpeg/issues/693)
- transition away from dev-utils [\#628](https://github.com/uclouvain/openjpeg/issues/628)
- update instructions to build with Visual Studio and 64-Bit Visual C++
  Toolset. [\#1028](https://github.com/uclouvain/openjpeg/pull/1028) ([quangnh89](https://github.com/quangnh89))
- Add missing newline at end of
  file [\#1024](https://github.com/uclouvain/openjpeg/pull/1024) ([stweil](https://github.com/stweil))
- merge master into coverity\_scan to update coverity
  results [\#1008](https://github.com/uclouvain/openjpeg/pull/1008) ([detonin](https://github.com/detonin))
- Use more const
  qualifiers [\#984](https://github.com/uclouvain/openjpeg/pull/984) ([stweil](https://github.com/stweil))
- Changes in converttif.c for
  PPC64 [\#980](https://github.com/uclouvain/openjpeg/pull/980) ([szukw000](https://github.com/szukw000))

## [v2.2.0](https://github.com/uclouvain/openjpeg/releases/v2.2.0) (2017-08-10)

[Full Changelog](https://github.com/uclouvain/openjpeg/compare/v2.1.2...v2.2.0)

**Implemented enhancements:**

- Memory consumption reduction at decoding
  side [\#968](https://github.com/uclouvain/openjpeg/pull/968) ([rouault](https://github.com/rouault))
- T1 & DWT multithreading decoding
  optimizations [\#786](https://github.com/uclouvain/openjpeg/pull/786) ([rouault](https://github.com/rouault))
- Tier1 decoder speed
  optimizations [\#783](https://github.com/uclouvain/openjpeg/pull/783) ([rouault](https://github.com/rouault))
- Inverse DWT 5x3: lift implementation / SSE accelerated
  version [\#953](https://github.com/uclouvain/openjpeg/issues/953)
- install static
  libraries [\#969](https://github.com/uclouvain/openjpeg/pull/969) ([jeroen](https://github.com/jeroen))
- IDWT 5x3 single-pass lifting and SSE2/AVX2
  implementation [\#957](https://github.com/uclouvain/openjpeg/pull/957) ([rouault](https://github.com/rouault))
- build both shared and static
  library [\#954](https://github.com/uclouvain/openjpeg/pull/954) ([jeroen](https://github.com/jeroen))
- T1 flag optimizations
  \(\#172\) [\#945](https://github.com/uclouvain/openjpeg/pull/945) ([rouault](https://github.com/rouault))
- CMake: add stronger warnings for openjp2 lib/bin by default, and error out on
  declaration-after-statement [\#936](https://github.com/uclouvain/openjpeg/pull/936) ([rouault](https://github.com/rouault))
- Quiet mode for opj\_decompress via -quiet long
  parameter. [\#928](https://github.com/uclouvain/openjpeg/pull/928) ([RussellMcOrmond](https://github.com/RussellMcOrmond))
- Implement predictive termination
  check [\#800](https://github.com/uclouvain/openjpeg/pull/800) ([rouault](https://github.com/rouault))

**Fixed bugs:**

- Several issues spotted by Google OSS
  Fuzz - [see here](https://github.com/search?l=&q=OSS+Fuzz+author-date%3A2017-07-04..2017-08-01+repo%3Auclouvain%2Fopenjpeg&ref=advsearch&type=Commits&utf8=%E2%9C%93)
- Missing fclose [\#976](https://github.com/uclouvain/openjpeg/issues/976)
- Heap buffer overflow read in openjpeg
  imagetopnm [\#970](https://github.com/uclouvain/openjpeg/issues/970)
- opj\_decompress opj\_j2k\_update\_image\_data\(\) Segment
  falut [\#948](https://github.com/uclouvain/openjpeg/issues/948)
- Generic Crash in 1.5.0 [\#941](https://github.com/uclouvain/openjpeg/issues/941)
- Segmentation Faults [\#940](https://github.com/uclouvain/openjpeg/issues/940)
- Assertions thrown [\#939](https://github.com/uclouvain/openjpeg/issues/939)
- Floating Point Errors [\#938](https://github.com/uclouvain/openjpeg/issues/938)
- Division by zero crash [\#937](https://github.com/uclouvain/openjpeg/issues/937)
- malformed jp2 can cause
  heap-buffer-overflow  [\#909](https://github.com/uclouvain/openjpeg/issues/909)
- NULL dereference can cause by malformed
  file [\#908](https://github.com/uclouvain/openjpeg/issues/908)
- Out of bound read in opj\_j2k\_add\_mct [\#907](https://github.com/uclouvain/openjpeg/issues/907)
- Check bpno\_plus\_one in
  opj\_t1\_decode\_cblk [\#903](https://github.com/uclouvain/openjpeg/issues/903)
- Undefined-shift in opj\_j2k\_read\_siz [\#902](https://github.com/uclouvain/openjpeg/issues/902)
- opj\_compress v2.1.2 can create images opj\_decompress cannot
  read [\#891](https://github.com/uclouvain/openjpeg/issues/891)
- Improper usage of opj\_int\_ceildiv can cause
  overflows [\#889](https://github.com/uclouvain/openjpeg/issues/889)
- Undefined shift in
  opj\_get\_all\_encoding\_parameters [\#885](https://github.com/uclouvain/openjpeg/issues/885)
- Denial of service \(crash\) due to use-after-free when decoding an illegal JPEG2000 image file
  v2.1.2 \(2017-04 [\#880](https://github.com/uclouvain/openjpeg/issues/880)
- Denial of service \(crash\) when decoding an illegal JPEG2000 image file v2.1.2
  \(2017-03\) [\#879](https://github.com/uclouvain/openjpeg/issues/879)
- bug png 2 j2k [\#868](https://github.com/uclouvain/openjpeg/issues/868)
- Inconsistent compression using cinema settings on folder of non-compliant
  image [\#864](https://github.com/uclouvain/openjpeg/issues/864)
- Openjpeg-2.1.2 Heap Buffer Overflow Vulnerability due to Insufficient
  check [\#862](https://github.com/uclouvain/openjpeg/issues/862)
- Heap Buffer Overflow in function pnmtoimage of
  convert.c [\#861](https://github.com/uclouvain/openjpeg/issues/861)
- CVE-2016-9112 FPE\(Floating Point Exception\) in lib/openjp2/pi.c:
  523 [\#855](https://github.com/uclouvain/openjpeg/issues/855)
- CVE-2016-5139, CVE-2016-5152, CVE-2016-5158,
  CVE-2016-5159 [\#854](https://github.com/uclouvain/openjpeg/issues/854)
- Undefined Reference error [\#853](https://github.com/uclouvain/openjpeg/issues/853)
- opj\_compress with lossy compression results in strange pixel
  values [\#851](https://github.com/uclouvain/openjpeg/issues/851)
- CVE-2016-1626 and CVE-2016-1628 [\#850](https://github.com/uclouvain/openjpeg/issues/850)
- Out-of-Bounds Write in opj\_mqc\_byteout of
  mqc.c [\#835](https://github.com/uclouvain/openjpeg/issues/835)
- WARNING in tgt\_create tree-\>numnodes == 0, no tree
  created. [\#794](https://github.com/uclouvain/openjpeg/issues/794)
- Potential overflow when precision is larger than
  32 [\#781](https://github.com/uclouvain/openjpeg/issues/781)
- division-by-zero in function opj\_pi\_next\_rpcl of pi.c \(line
  366\) [\#780](https://github.com/uclouvain/openjpeg/issues/780)
- division-by-zero in function opj\_pi\_next\_rpcl of pi.c \(line
  363\) [\#779](https://github.com/uclouvain/openjpeg/issues/779)
- division-by-zero in function opj\_pi\_next\_pcrl of pi.c \(line
  447\) [\#778](https://github.com/uclouvain/openjpeg/issues/778)
- division-by-zero in function opj\_pi\_next\_pcrl of pi.c \(line
  444\) [\#777](https://github.com/uclouvain/openjpeg/issues/777)
- Encoding the following file with 32x32 tiling produces jp2 image with
  artifact [\#737](https://github.com/uclouvain/openjpeg/issues/737)
- division-by-zero \(SIGFPE\) error in opj\_pi\_next\_cprl function \(line 526 of
  pi.c\) [\#732](https://github.com/uclouvain/openjpeg/issues/732)
- division-by-zero \(SIGFPE\) error in opj\_pi\_next\_cprl function \(line 523 of
  pi.c\) [\#731](https://github.com/uclouvain/openjpeg/issues/731)
- OpenJpeg 2.1 and 1.4 fails to decompress this file
  correctly [\#721](https://github.com/uclouvain/openjpeg/issues/721)
- MQ Encode :uninitialized memory access when first pass does not output any
  bytes [\#709](https://github.com/uclouvain/openjpeg/issues/709)
- Out-of-bounds read in opj\_j2k\_update\_image\_data and opj\_tgt\_reset
  function [\#704](https://github.com/uclouvain/openjpeg/issues/704)
- Remove opj\_aligned\_malloc / opj\_aligned\_realloc /
  opj\_aligned\_free? [\#689](https://github.com/uclouvain/openjpeg/issues/689)
- There is an issue with rendering some type of jpeg file. Please ref the
  link. [\#672](https://github.com/uclouvain/openjpeg/issues/672)
- Null Dereference in
  tcd\_malloc\_decode\_tile [\#657](https://github.com/uclouvain/openjpeg/issues/657)
- ETS-C1P0-p0\_12.j2k-compare2ref & NR-C1P0-p0\_12.j2k-compare2base failing under
  windows [\#655](https://github.com/uclouvain/openjpeg/issues/655)
- Memory leak [\#631](https://github.com/uclouvain/openjpeg/issues/631)
- Test 481 reports error in valgrind
  memcheck [\#612](https://github.com/uclouvain/openjpeg/issues/612)
- reserved identifier violation [\#587](https://github.com/uclouvain/openjpeg/issues/587)
- Buffer overflow when compressing some 16 bits images of the test
  suite [\#539](https://github.com/uclouvain/openjpeg/issues/539)
- Heap-buffer-overflow in
  opj\_dwt\_decode\_1 [\#480](https://github.com/uclouvain/openjpeg/issues/480)
- Automated fuzz testing [\#468](https://github.com/uclouvain/openjpeg/issues/468)
- Expected to find EPH marker  [\#425](https://github.com/uclouvain/openjpeg/issues/425)
- read: segment too long \(6182\) with max \(35872\) for codeblock 0 \(p=19, b=2, r=5,
  c=1\) [\#284](https://github.com/uclouvain/openjpeg/issues/284)
- building 64bit version has lots of
  warnings [\#244](https://github.com/uclouvain/openjpeg/issues/244)
- Wrong encoding of small tiles with high level
  number [\#239](https://github.com/uclouvain/openjpeg/issues/239)
- Errors raised in pi.c by VS11 analyzer  [\#190](https://github.com/uclouvain/openjpeg/issues/190)
- Undocumented optimization found in v2 branch of
  openjpeg [\#183](https://github.com/uclouvain/openjpeg/issues/183)
- T1 optimisations jpeg2000 [\#172](https://github.com/uclouvain/openjpeg/issues/172)
- Remove OPJ\_NOSANITIZE in opj\_bio\_read\(\) and opj\_bio\_write\(\)
  \(\#761\) [\#955](https://github.com/uclouvain/openjpeg/pull/955) ([rouault](https://github.com/rouault))
- Fix bypass pterm termall and lossless decomposition issue \(\#891,
  \#892\) [\#949](https://github.com/uclouvain/openjpeg/pull/949) ([rouault](https://github.com/rouault))
- Escape quotes to ensure README renders on GitHub
  correctly [\#914](https://github.com/uclouvain/openjpeg/pull/914) ([alexwlchan](https://github.com/alexwlchan))
- Remove spurious .R macros from
  manpages [\#899](https://github.com/uclouvain/openjpeg/pull/899) ([jwilk](https://github.com/jwilk))
- Remove warnings related to empty
  tag-trees. [\#893](https://github.com/uclouvain/openjpeg/pull/893) ([rouault](https://github.com/rouault))

**Maintenance-related tasks:**

- Submit OpenJPEG to oss-fuzz [\#965](https://github.com/uclouvain/openjpeg/issues/965)
- Updates for Doxygen to suppress warnings [\#849](https://github.com/uclouvain/openjpeg/issues/849)
- Remove useless knownfailures \(since LAZY encoding is
  fixed\) [\#964](https://github.com/uclouvain/openjpeg/pull/964) ([rouault](https://github.com/rouault))
- Enable AVX2 at runtime on Travis-CI and
  AppVeyor [\#963](https://github.com/uclouvain/openjpeg/pull/963) ([rouault](https://github.com/rouault))
- Tests: test opj\_compress in VSC mode \(related to
  \#172\) [\#935](https://github.com/uclouvain/openjpeg/pull/935) ([rouault](https://github.com/rouault))
- Reformat: apply reformattin on .h files
  \(\#128\) [\#926](https://github.com/uclouvain/openjpeg/pull/926) ([rouault](https://github.com/rouault))
- Add mechanisms to reformat and check code style, and reformat whole codebase
  \(\#128\) [\#919](https://github.com/uclouvain/openjpeg/pull/919) ([rouault](https://github.com/rouault))
- Add profiling of CPU and memory usage
  \(\#912\) [\#918](https://github.com/uclouvain/openjpeg/pull/918) ([rouault](https://github.com/rouault))
- Add performance benchmarking
  scripts [\#917](https://github.com/uclouvain/openjpeg/pull/917) ([rouault](https://github.com/rouault))
- Fix retrieval of jpylyzer in
  AppVeyor [\#915](https://github.com/uclouvain/openjpeg/pull/915) ([rouault](https://github.com/rouault))

## [v2.1.2](https://github.com/uclouvain/openjpeg/releases/v2.1.2) (2016-09-28)

[Full Changelog](https://github.com/uclouvain/openjpeg/compare/v2.1.1...v2.1.2)

**Closed issues:**

- null ptr dereference in convert.c:1331 [\#843](https://github.com/uclouvain/openjpeg/issues/843)
- Out-of-Bounds Read in function bmp24toimage of
  convertbmp.c [\#833](https://github.com/uclouvain/openjpeg/issues/833)
- Disable automatic compilation of t1\_generate\_luts in
  CMakeLists.txt [\#831](https://github.com/uclouvain/openjpeg/issues/831)
- CVE-2016-7163 Integer overflow in
  opj\_pi\_create\_decode [\#826](https://github.com/uclouvain/openjpeg/issues/826)
- Security Advisory for OpenJPEG [\#810](https://github.com/uclouvain/openjpeg/issues/810)
- Add dashboard with static lib [\#804](https://github.com/uclouvain/openjpeg/issues/804)
- hidden visibility for the static library / building with -DOPJ\_STATIC against shared
  lib [\#802](https://github.com/uclouvain/openjpeg/issues/802)
- Optimization when building library from
  source [\#799](https://github.com/uclouvain/openjpeg/issues/799)
- unsigned int16 on Solaris 11.2/sparc [\#796](https://github.com/uclouvain/openjpeg/issues/796)
- appveyor [\#793](https://github.com/uclouvain/openjpeg/issues/793)
- FFMpeg will not link to 2.1.1 release built as shared
  library [\#766](https://github.com/uclouvain/openjpeg/issues/766)
- API change since v2: opj\_event\_mgr\_t not
  available [\#754](https://github.com/uclouvain/openjpeg/issues/754)
- openjpeg.h needs dependencies [\#673](https://github.com/uclouvain/openjpeg/issues/673)
- "master" does not build on ubuntu [\#658](https://github.com/uclouvain/openjpeg/issues/658)
- Package 'openjp2', required by 'libopenjpip', not
  found [\#594](https://github.com/uclouvain/openjpeg/issues/594)

**Merged pull requests:**

- Fix PNM file
  reading [\#847](https://github.com/uclouvain/openjpeg/pull/847) ([mayeut](https://github.com/mayeut))
- Fix some issues reported by Coverity
  Scan [\#846](https://github.com/uclouvain/openjpeg/pull/846) ([stweil](https://github.com/stweil))
- Fix potential out-of-bounds read
  \(coverity\)  [\#844](https://github.com/uclouvain/openjpeg/pull/844) ([stweil](https://github.com/stweil))
- Remove TODO for overflow
  check [\#842](https://github.com/uclouvain/openjpeg/pull/842) ([mayeut](https://github.com/mayeut))
- Add overflow checks for
  opj\_aligned\_malloc [\#841](https://github.com/uclouvain/openjpeg/pull/841) ([mayeut](https://github.com/mayeut))
- Flags in T1 shall be
  unsigned [\#840](https://github.com/uclouvain/openjpeg/pull/840) ([mayeut](https://github.com/mayeut))
- Fix some
  warnings [\#838](https://github.com/uclouvain/openjpeg/pull/838) ([mayeut](https://github.com/mayeut))
- Fix issue
    833. [\#834](https://github.com/uclouvain/openjpeg/pull/834) ([trylab](https://github.com/trylab))
- Add overflow checks for
  opj\_aligned\_malloc [\#832](https://github.com/uclouvain/openjpeg/pull/832) ([mayeut](https://github.com/mayeut))
- Add test for issue
  820 [\#829](https://github.com/uclouvain/openjpeg/pull/829) ([mayeut](https://github.com/mayeut))
- Add test for issue
  826 [\#827](https://github.com/uclouvain/openjpeg/pull/827) ([mayeut](https://github.com/mayeut))
- Fix coverity 113065
  \(CWE-484\) [\#824](https://github.com/uclouvain/openjpeg/pull/824) ([mayeut](https://github.com/mayeut))
- Add sanity check for tile
  coordinates [\#823](https://github.com/uclouvain/openjpeg/pull/823) ([mayeut](https://github.com/mayeut))
- Add test for PR
  818 [\#822](https://github.com/uclouvain/openjpeg/pull/822) ([mayeut](https://github.com/mayeut))
- Update to libpng
  1.6.25 [\#821](https://github.com/uclouvain/openjpeg/pull/821) ([mayeut](https://github.com/mayeut))
- CVE-2016-8332: fix incrementing of "l\_tcp-\>m\_nb\_mcc\_records" in
  opj\_j2k\_read\_mcc [\#820](https://github.com/uclouvain/openjpeg/pull/820) ([mayeut](https://github.com/mayeut))
- Add overflow check in
  opj\_tcd\_init\_tile [\#819](https://github.com/uclouvain/openjpeg/pull/819) ([mayeut](https://github.com/mayeut))
- Fix leak & invalid behavior of
  opj\_jp2\_read\_ihdr [\#818](https://github.com/uclouvain/openjpeg/pull/818) ([mayeut](https://github.com/mayeut))
- Add overflow check in
  opj\_j2k\_update\_image\_data [\#817](https://github.com/uclouvain/openjpeg/pull/817) ([mayeut](https://github.com/mayeut))
- Change 'restrict' define to '
  OPJ\_RESTRICT' [\#816](https://github.com/uclouvain/openjpeg/pull/816) ([mayeut](https://github.com/mayeut))
- Switch to clang
  3.8 [\#814](https://github.com/uclouvain/openjpeg/pull/814) ([mayeut](https://github.com/mayeut))
- Fix an integer overflow
  issue [\#809](https://github.com/uclouvain/openjpeg/pull/809) ([trylab](https://github.com/trylab))
- Update to lcms
  2.8 [\#808](https://github.com/uclouvain/openjpeg/pull/808) ([mayeut](https://github.com/mayeut))
- Update to libpng
  1.6.24 [\#807](https://github.com/uclouvain/openjpeg/pull/807) ([mayeut](https://github.com/mayeut))
- Reenable clang-3.9 build on
  travis [\#806](https://github.com/uclouvain/openjpeg/pull/806) ([mayeut](https://github.com/mayeut))
- Bit fields
  type [\#805](https://github.com/uclouvain/openjpeg/pull/805) ([smuehlst](https://github.com/smuehlst))
- Add compilation test for standalone inclusion of
  openjpeg.h [\#798](https://github.com/uclouvain/openjpeg/pull/798) ([mayeut](https://github.com/mayeut))
- jpwl: Remove non-portable data type u\_int16\_t \(fix issue
  \#796\) [\#797](https://github.com/uclouvain/openjpeg/pull/797) ([stweil](https://github.com/stweil))
- Fix dependency for pkg-config \(issue
  \#594\) [\#795](https://github.com/uclouvain/openjpeg/pull/795) ([stweil](https://github.com/stweil))
- Add
  .gitignore [\#787](https://github.com/uclouvain/openjpeg/pull/787) ([stweil](https://github.com/stweil))

## [v2.1.1](https://github.com/uclouvain/openjpeg/releases/tag/v2.1.1) (2016-07-05)

[Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.2.1...v2.1.1)

**Implemented enhancements:**

- opj\_malloc replacement [\#625](https://github.com/uclouvain/openjpeg/issues/625)
- backport "-p" and "-force-rgb" options in
  1.5 [\#606](https://github.com/uclouvain/openjpeg/issues/606)
- Use travis-ci matrix build [\#581](https://github.com/uclouvain/openjpeg/issues/581)
- Add Coverity Scan analysis [\#580](https://github.com/uclouvain/openjpeg/issues/580)
- Unnecessary rate distortion
  calculations  [\#479](https://github.com/uclouvain/openjpeg/issues/479)
- Add images from various security issues to test
  suite [\#415](https://github.com/uclouvain/openjpeg/issues/415)
- Coding speed for 9/7 on 32bits platforms \(x86/ARM\) can be improved with a quick
  fix [\#220](https://github.com/uclouvain/openjpeg/issues/220)

**Fixed bugs:**

- Out-of-Bounds Access in function opj\_tgt\_reset of
  tgt.c [\#775](https://github.com/uclouvain/openjpeg/issues/775)
- Heap Buffer Overflow in function color\_cmyk\_to\_rgb of
  color.c [\#774](https://github.com/uclouvain/openjpeg/issues/774)
- division-by-zero \(SIGFPE\) error in opj\_tcd\_init\_tile function \(line 730 of
  tcd.c\) [\#733](https://github.com/uclouvain/openjpeg/issues/733)
- Out-Of-Bounds Read in sycc422\_to\_rgb
  function [\#726](https://github.com/uclouvain/openjpeg/issues/726)
- Heap Corruption in opj\_free function [\#725](https://github.com/uclouvain/openjpeg/issues/725)
- Out-Of-Bounds Read in opj\_tcd\_free\_tile
  function [\#724](https://github.com/uclouvain/openjpeg/issues/724)
- Cannot handle box of undefined size [\#653](https://github.com/uclouvain/openjpeg/issues/653)
- Compilation fails without platform-supplied aligned
  malloc [\#642](https://github.com/uclouvain/openjpeg/issues/642)
- HP compiler warns about redeclaration of static
  function [\#640](https://github.com/uclouvain/openjpeg/issues/640)
- Implementation-defined behavior of malloc causes different behavior on Linux and
  AIX [\#635](https://github.com/uclouvain/openjpeg/issues/635)
- Build on AIX fails because "opj\_includes.h" is included after system
  headers [\#633](https://github.com/uclouvain/openjpeg/issues/633)
- Compiling with SSE2 on Linux 32-bit causes crashes in
  OpenJPEG [\#624](https://github.com/uclouvain/openjpeg/issues/624)
- Build on AIX fails because of "restrict"
  pointers [\#620](https://github.com/uclouvain/openjpeg/issues/620)
- bug in new tif conversion code [\#609](https://github.com/uclouvain/openjpeg/issues/609)
- bin/jp2/convert.c line 1085 Resource
  leak [\#607](https://github.com/uclouvain/openjpeg/issues/607)
- bin/jp2/convert.c memory leak [\#601](https://github.com/uclouvain/openjpeg/issues/601)
- Resource leak in opj\_j2k\_create\_cstr\_index in case of
  failure [\#599](https://github.com/uclouvain/openjpeg/issues/599)
- Resource leak in opj\_j2k\_encode in case of
  failure [\#598](https://github.com/uclouvain/openjpeg/issues/598)
- Resource leak in opj\_j2k\_decode\_one\_tile in case of
  failure [\#597](https://github.com/uclouvain/openjpeg/issues/597)
- Resource Leak [\#573](https://github.com/uclouvain/openjpeg/issues/573)
- opj\_compress fails to compress lossless on gcc/x86
  \(-m32\) [\#571](https://github.com/uclouvain/openjpeg/issues/571)
- Use-after-free in opj\_j2k\_write\_mco [\#563](https://github.com/uclouvain/openjpeg/issues/563)
- openjpeg-master-2015-07-30 failed to compile on
  LINUX [\#556](https://github.com/uclouvain/openjpeg/issues/556)
- PNG images are always read as RGB\(A\)
  images [\#536](https://github.com/uclouvain/openjpeg/issues/536)
- g4\_colr.j2c not handled properly [\#532](https://github.com/uclouvain/openjpeg/issues/532)
- Bigendian: opj\_compress + opj\_decompress
  fails [\#518](https://github.com/uclouvain/openjpeg/issues/518)
- Suspicious code in j2k.c [\#517](https://github.com/uclouvain/openjpeg/issues/517)
- Decode times almost double\(!!\) on Visual Studio 2013,
  2015 [\#505](https://github.com/uclouvain/openjpeg/issues/505)
-

opj\_data/input/nonregression/issue226.j2k [\#500](https://github.com/uclouvain/openjpeg/issues/500)

- opj\_setup\_encoder always returns true [\#497](https://github.com/uclouvain/openjpeg/issues/497)
- Double free in j2k\_read\_ppm\_v3 parsing \(\(presumably invalid\)
  image. [\#496](https://github.com/uclouvain/openjpeg/issues/496)
- Invalid write in
  opj\_j2k\_update\_image\_data [\#495](https://github.com/uclouvain/openjpeg/issues/495)
- Undefined printf format specifier %ud used in
  code [\#494](https://github.com/uclouvain/openjpeg/issues/494)
- Potential double free on malloc failure in
  opj\_j2k\_copy\_default\_tcp\_and\_create\_tcp\(\) [\#492](https://github.com/uclouvain/openjpeg/issues/492)
- Do not link with -ffast-math [\#488](https://github.com/uclouvain/openjpeg/issues/488)
- Heap-buffer-overflow in opj\_dwt\_decode [\#486](https://github.com/uclouvain/openjpeg/issues/486)
- opj\_dump fails on Windows 7, 64 bits [\#482](https://github.com/uclouvain/openjpeg/issues/482)
- SIGSEGV in opj\_j2k\_update\_image\_data via
  pdfium\_test [\#481](https://github.com/uclouvain/openjpeg/issues/481)
- Heap-buffer-overflow in
  opj\_j2k\_tcp\_destroy [\#477](https://github.com/uclouvain/openjpeg/issues/477)
- Invalid image causes write past end of heap
  buffer [\#476](https://github.com/uclouvain/openjpeg/issues/476)
- Assertion `l\_res-\>x0 \>= 0' fails when parsing invalid
  images  [\#475](https://github.com/uclouvain/openjpeg/issues/475)
- Bug on opj\_write\_bytes\_BE function  [\#472](https://github.com/uclouvain/openjpeg/issues/472)
- Refactor j2k\_read\_ppm\_v3 function [\#470](https://github.com/uclouvain/openjpeg/issues/470)
- compression: strange precinct dimensions [\#466](https://github.com/uclouvain/openjpeg/issues/466)
- \(:- Console message in
  opj\_decompress -:\) [\#465](https://github.com/uclouvain/openjpeg/issues/465)
- opj\_decompress fails to decompress any
  files [\#463](https://github.com/uclouvain/openjpeg/issues/463)
- bio-\>ct is unnecessarily set to zero in opj\_bio\_flush
  method [\#461](https://github.com/uclouvain/openjpeg/issues/461)
- Maximal unsigned short is 65535, not
  65536 [\#460](https://github.com/uclouvain/openjpeg/issues/460)
- OpenJpeg fails to encode components with different precision
  properly [\#459](https://github.com/uclouvain/openjpeg/issues/459)
- component precision upscaling isn't correct in
  opj\_decompress [\#458](https://github.com/uclouvain/openjpeg/issues/458)
- Multiple precision components won't get encoded to jp2 if 1 component is unsigned 1
  bit [\#457](https://github.com/uclouvain/openjpeg/issues/457)
- Incorrect code in ../bin/jp2/convert.c, function
  rawtoimage\_common\(...\) [\#456](https://github.com/uclouvain/openjpeg/issues/456)
- \[OpenJPEG-trunk\] opj\_stream\_get\_number\_byte\_left throws
  assert [\#455](https://github.com/uclouvain/openjpeg/issues/455)
- NR-DEC-kodak\_2layers\_lrcp.j2c-31-decode-md5 fails randomly when running tests in
  parallel [\#454](https://github.com/uclouvain/openjpeg/issues/454)
- compare\_raw\_files doesn't report an error on invalid arguments / missing input
  files [\#453](https://github.com/uclouvain/openjpeg/issues/453)
- Forward discrete wavelet transform: implement periodic symmetric extension at
  boundaries [\#452](https://github.com/uclouvain/openjpeg/issues/452)
- Bug in tiff reading method in convert.c [\#449](https://github.com/uclouvain/openjpeg/issues/449)
- Image in pdf don't display [\#447](https://github.com/uclouvain/openjpeg/issues/447)
- Multiple issues causing opj\_decompress to
  segfault [\#446](https://github.com/uclouvain/openjpeg/issues/446)
- opj\_compress: 40% of encode time is spent freeing
  data [\#445](https://github.com/uclouvain/openjpeg/issues/445)
- Multiple warnings when configuring OpenJPEG on MacOS with CMake 3.x
  \(trunk\) [\#443](https://github.com/uclouvain/openjpeg/issues/443)
- valgrind memleak found [\#437](https://github.com/uclouvain/openjpeg/issues/437)
- global-buffer-overflow src/lib/openjp2/t1.c:1146
  opj\_t1\_getwmsedec [\#436](https://github.com/uclouvain/openjpeg/issues/436)
- Warning introduced on trunk r2923 &
  r2924 [\#435](https://github.com/uclouvain/openjpeg/issues/435)
- heap-buffer-overflow in
  opj\_t1\_decode\_cblks [\#432](https://github.com/uclouvain/openjpeg/issues/432)
- Heap-buffer-overflow in
  opj\_tcd\_init\_decode\_tile [\#431](https://github.com/uclouvain/openjpeg/issues/431)
- Heap-buffer-overflow in
  opj\_j2k\_tcp\_destroy [\#430](https://github.com/uclouvain/openjpeg/issues/430)
- Heap-buffer-overflow in
  opj\_jp2\_apply\_pclr [\#429](https://github.com/uclouvain/openjpeg/issues/429)
- issue412 revisited [\#428](https://github.com/uclouvain/openjpeg/issues/428)
- Image distorted \(sides look cankered\) [\#423](https://github.com/uclouvain/openjpeg/issues/423)
- openjpeg-2.x-trunk-r2918 is broken in
  color.c [\#422](https://github.com/uclouvain/openjpeg/issues/422)
- Heap-buffer-overflow in
  opj\_tcd\_init\_decode\_tile [\#420](https://github.com/uclouvain/openjpeg/issues/420)
- Heap-use-after-free in
  opj\_t1\_decode\_cblks [\#418](https://github.com/uclouvain/openjpeg/issues/418)
- UNKNOWN in opj\_read\_bytes\_LE [\#417](https://github.com/uclouvain/openjpeg/issues/417)
- Transparency problem [\#416](https://github.com/uclouvain/openjpeg/issues/416)
- Image with per channel alpha \(cdef\) does not decode
  properly [\#414](https://github.com/uclouvain/openjpeg/issues/414)
- OpenJPEG crashes with attached image [\#413](https://github.com/uclouvain/openjpeg/issues/413)
- Palette image with cdef fails to
  decompress [\#412](https://github.com/uclouvain/openjpeg/issues/412)
- Invalid member values from opj\_read\_header or
  opj\_decode ? [\#411](https://github.com/uclouvain/openjpeg/issues/411)
- MD5 Checksum hangs under valgrind on MacOS
  X [\#410](https://github.com/uclouvain/openjpeg/issues/410)
- Heap-buffer-overflow in
  opj\_tcd\_get\_decoded\_tile\_size [\#408](https://github.com/uclouvain/openjpeg/issues/408)
- C++ style comments in
  trunk/src/lib/openjp2/j2k.c [\#407](https://github.com/uclouvain/openjpeg/issues/407)
- Backport bugfixes from trunk to 2.1
  branch [\#405](https://github.com/uclouvain/openjpeg/issues/405)
- Heap-buffer-overflow in
  parse\_cmdline\_encoder [\#403](https://github.com/uclouvain/openjpeg/issues/403)
- Heap-buffer-overflow in
  opj\_v4dwt\_interleave\_h [\#400](https://github.com/uclouvain/openjpeg/issues/400)
- Heap-buffer-overflow in opj\_dwt\_decode [\#399](https://github.com/uclouvain/openjpeg/issues/399)
- Heap-use-after-free in
  opj\_t1\_decode\_cblks [\#398](https://github.com/uclouvain/openjpeg/issues/398)
- Heap-buffer-overflow in
  opj\_jp2\_apply\_cdef [\#397](https://github.com/uclouvain/openjpeg/issues/397)
- Heap-buffer-overflow in
  opj\_t2\_read\_packet\_header [\#396](https://github.com/uclouvain/openjpeg/issues/396)
- Heap-buffer-overflow in
  opj\_t2\_read\_packet\_header [\#395](https://github.com/uclouvain/openjpeg/issues/395)
- Heap-buffer-overflow in
  opj\_dwt\_decode\_1 [\#394](https://github.com/uclouvain/openjpeg/issues/394)
- Heap-double-free in j2k\_read\_ppm\_v3 [\#393](https://github.com/uclouvain/openjpeg/issues/393)
- Security hole in j2k.c [\#392](https://github.com/uclouvain/openjpeg/issues/392)
- Security: double-free in
  opj\_tcd\_code\_block\_dec\_deallocate [\#391](https://github.com/uclouvain/openjpeg/issues/391)
- check for negative-size params in code [\#390](https://github.com/uclouvain/openjpeg/issues/390)
- Heap-buffer-overflow in
  opj\_t2\_read\_packet\_header [\#389](https://github.com/uclouvain/openjpeg/issues/389)
- Heap overflow in OpenJpeg 1.5.2 [\#388](https://github.com/uclouvain/openjpeg/issues/388)
- openjpip.so.6 file too short [\#387](https://github.com/uclouvain/openjpeg/issues/387)
- Corrupted JP3D file [\#386](https://github.com/uclouvain/openjpeg/issues/386)
- variable assigned to itself [\#383](https://github.com/uclouvain/openjpeg/issues/383)
- Null pointer dereferencing [\#382](https://github.com/uclouvain/openjpeg/issues/382)
- bad use of case statement [\#381](https://github.com/uclouvain/openjpeg/issues/381)
- Release 2.1 as a Ubuntu package [\#380](https://github.com/uclouvain/openjpeg/issues/380)
- Bug in libopenjpwl.pc [\#374](https://github.com/uclouvain/openjpeg/issues/374)
- inconsistent tile numbering in decode output
  message [\#370](https://github.com/uclouvain/openjpeg/issues/370)
- error in code block calculations [\#369](https://github.com/uclouvain/openjpeg/issues/369)
- r2872 fails to compile due to "attempt to use poisoned malloc" error in
  j2k.c [\#368](https://github.com/uclouvain/openjpeg/issues/368)
- OSX build gives libopenjp2.6.dylib with not-absolute install name
  id  [\#367](https://github.com/uclouvain/openjpeg/issues/367)
- opj\_decompress gives error but successfully decompress in OPJ
  2.1 [\#366](https://github.com/uclouvain/openjpeg/issues/366)
- pngtoimage\(\) and imagetopng\(\) have wrong byte order for 16-Bit
  image [\#365](https://github.com/uclouvain/openjpeg/issues/365)
- PDF crash in chrome - part2 \(due to attachment
  limit\) [\#364](https://github.com/uclouvain/openjpeg/issues/364)
- PDF crash in chrome - part1 [\#363](https://github.com/uclouvain/openjpeg/issues/363)
- PDF crash in chrome - part0 [\#362](https://github.com/uclouvain/openjpeg/issues/362)
- Compilation fails on Windows with mingw32
  gcc4.8 [\#361](https://github.com/uclouvain/openjpeg/issues/361)
- security issue [\#360](https://github.com/uclouvain/openjpeg/issues/360)
- improve memory management [\#359](https://github.com/uclouvain/openjpeg/issues/359)
- how to compress a yuv420 raw data using
  opj\_compress [\#357](https://github.com/uclouvain/openjpeg/issues/357)
- Some memory allocation are not checked [\#355](https://github.com/uclouvain/openjpeg/issues/355)
- Static library symbols shall be marked as
  hidden [\#354](https://github.com/uclouvain/openjpeg/issues/354)
- opj\_compress rejects valid bmp files [\#353](https://github.com/uclouvain/openjpeg/issues/353)
- opj\_compress crashes when number of resolutions is set to
  zero [\#352](https://github.com/uclouvain/openjpeg/issues/352)
- Compilation error under Visual Studio
  2003 [\#351](https://github.com/uclouvain/openjpeg/issues/351)
- opj\_compress description example error \[Low
  priority\] [\#350](https://github.com/uclouvain/openjpeg/issues/350)
- opj\_write\_bytes\_BE is wrong in trunk [\#345](https://github.com/uclouvain/openjpeg/issues/345)
- PART1ONLY option in release.sh doesn't work
  properly [\#332](https://github.com/uclouvain/openjpeg/issues/332)
- openjpeg crash error [\#330](https://github.com/uclouvain/openjpeg/issues/330)
- openjpeg decompress error [\#329](https://github.com/uclouvain/openjpeg/issues/329)
- openjpeg decompress issue [\#326](https://github.com/uclouvain/openjpeg/issues/326)
- limited tif support [\#322](https://github.com/uclouvain/openjpeg/issues/322)
- asoc value of 65536 is allowed [\#321](https://github.com/uclouvain/openjpeg/issues/321)
- opj\_skip\_from\_file error [\#314](https://github.com/uclouvain/openjpeg/issues/314)
- Heavy quota usage in openjpeg [\#309](https://github.com/uclouvain/openjpeg/issues/309)
- Verify -help actually match letter [\#307](https://github.com/uclouvain/openjpeg/issues/307)
- g3\_colr.j2c not handled [\#288](https://github.com/uclouvain/openjpeg/issues/288)
- reopen/fix issue 165 [\#280](https://github.com/uclouvain/openjpeg/issues/280)
- kakadu conformance tests [\#279](https://github.com/uclouvain/openjpeg/issues/279)
- missing break after case statement in
  opj\_dwt\_decode\_real  [\#274](https://github.com/uclouvain/openjpeg/issues/274)
- Run Coverity on trunk [\#270](https://github.com/uclouvain/openjpeg/issues/270)
- NR-ENC-random-issue-0005.tif-12-encode [\#259](https://github.com/uclouvain/openjpeg/issues/259)
- Use new add\_test signature to handle cross
  compilation [\#258](https://github.com/uclouvain/openjpeg/issues/258)
- Loss decoding quality in 2.0.0 [\#254](https://github.com/uclouvain/openjpeg/issues/254)
- Decompress that worked in 1.5.1 fails in
  2.0 [\#252](https://github.com/uclouvain/openjpeg/issues/252)
- Expected endianness with raw input is not documented leading to
  SEGFAULT [\#251](https://github.com/uclouvain/openjpeg/issues/251)
- OpenJPEG writes to stderr [\#246](https://github.com/uclouvain/openjpeg/issues/246)
- Inconsistent logging of tile index [\#245](https://github.com/uclouvain/openjpeg/issues/245)
- patch for openjpeg-trunk-r2347 and
  BIG\_ENDIAN [\#242](https://github.com/uclouvain/openjpeg/issues/242)
- CMAP: MTYP == 0 \(direct use\) not handled
  properly [\#235](https://github.com/uclouvain/openjpeg/issues/235)
- Black Pixel [\#233](https://github.com/uclouvain/openjpeg/issues/233)
- opj\_compress runtime error after fresh Linux install due to apparent failure to execute
  ldconfig [\#219](https://github.com/uclouvain/openjpeg/issues/219)
- openjp2 debug works, release build does
  not [\#217](https://github.com/uclouvain/openjpeg/issues/217)
- openjpeg-branch15-r2299 and openjpeg-trunk-r2299 fail to decode a JP2
  file [\#212](https://github.com/uclouvain/openjpeg/issues/212)
- openjpeg-trunk issue with Win7 [\#201](https://github.com/uclouvain/openjpeg/issues/201)
- undefined reference to `opj\_version' [\#200](https://github.com/uclouvain/openjpeg/issues/200)
- In tgt.c we used fprintf not the openjpeg message
  reporter [\#184](https://github.com/uclouvain/openjpeg/issues/184)
- Windows binaries not working under WinXP [\#176](https://github.com/uclouvain/openjpeg/issues/176)
- add ability to use intel ipp \(performance primitive\) within
  OpenJPEG [\#164](https://github.com/uclouvain/openjpeg/issues/164)
- Migration guide v2 [\#160](https://github.com/uclouvain/openjpeg/issues/160)
- Cannot decompress
  JPEG2000Aware3.18.7.3Win32\_kdutranscode6.3.1.j2k [\#158](https://github.com/uclouvain/openjpeg/issues/158)
- Cannot decompress
  JPEG2000Aware3.18.7.3Win32.j2k [\#157](https://github.com/uclouvain/openjpeg/issues/157)
- openjpeg@googlegroups.com has
  disappeared [\#153](https://github.com/uclouvain/openjpeg/issues/153)
- OpenJPEG 1.5.0 crashes on a ridiculously big
  file... [\#151](https://github.com/uclouvain/openjpeg/issues/151)
- opj\_image vs free [\#146](https://github.com/uclouvain/openjpeg/issues/146)
- Windows .dll file invalid [\#140](https://github.com/uclouvain/openjpeg/issues/140)
- Problem with second layer of a 2 layer coded LRCP \(with
  precincts\) [\#135](https://github.com/uclouvain/openjpeg/issues/135)
- version 1.4 crashes when opening PDF file with JPEG2000
  images [\#133](https://github.com/uclouvain/openjpeg/issues/133)
- Setup a win64 dashboard [\#132](https://github.com/uclouvain/openjpeg/issues/132)
- J2KP4files/codestreams\_profile0/p0\_13.j2k question
  jpeg2000 [\#131](https://github.com/uclouvain/openjpeg/issues/131)
- Out of memory: Kill process 11204 \(opj\_server\) score 917 or sacrifice
  child [\#123](https://github.com/uclouvain/openjpeg/issues/123)
- FILE\* in opj API is unsafe [\#120](https://github.com/uclouvain/openjpeg/issues/120)
- third-party lib order [\#119](https://github.com/uclouvain/openjpeg/issues/119)
- openjpeg-1.5.0-Darwin-powerpc.dmg is
  huge ! [\#113](https://github.com/uclouvain/openjpeg/issues/113)
- misleading info in JP2 box lead to wrong number of
  components [\#110](https://github.com/uclouvain/openjpeg/issues/110)
- Image\_to\_j2k says that j2k files is generated but no file is on the
  HDD [\#109](https://github.com/uclouvain/openjpeg/issues/109)
- Error in openjpegV1.4 on compiling image\_to\_j2k: crash on reading bmp
  file [\#108](https://github.com/uclouvain/openjpeg/issues/108)
- Update to abi-compliance-checker 1.96 [\#106](https://github.com/uclouvain/openjpeg/issues/106)
- Decode error on the attached JPEG...works in KDU and with JASPER...please
  help! [\#101](https://github.com/uclouvain/openjpeg/issues/101)
- Mac binaries v1.4 is broken [\#95](https://github.com/uclouvain/openjpeg/issues/95)
- jp2\_read\_boxhdr\(\) has size bug in version
  1 [\#92](https://github.com/uclouvain/openjpeg/issues/92)
- Support for Java JAI Imageio [\#90](https://github.com/uclouvain/openjpeg/issues/90)
- encoding test failing [\#86](https://github.com/uclouvain/openjpeg/issues/86)
- source archive on demand [\#85](https://github.com/uclouvain/openjpeg/issues/85)
- CMakeLists.txt and Makefile.am for JPIP are
  buggy [\#84](https://github.com/uclouvain/openjpeg/issues/84)
- pclr-cmap-cdef [\#82](https://github.com/uclouvain/openjpeg/issues/82)
- Error when compiling
  openjpeg\_v1\_4\_sources\_r697 [\#79](https://github.com/uclouvain/openjpeg/issues/79)
- J2K codec issue on Windows Mobile  [\#77](https://github.com/uclouvain/openjpeg/issues/77)
- image\_to\_j2k.exe crashes on large .bmp
  file [\#75](https://github.com/uclouvain/openjpeg/issues/75)
- fatal error C1900 building the project on
  windows [\#65](https://github.com/uclouvain/openjpeg/issues/65)
- same option but different size [\#54](https://github.com/uclouvain/openjpeg/issues/54)
- Missing openjpegConfigure.h [\#38](https://github.com/uclouvain/openjpeg/issues/38)
- Not an issue in openjpeg, but ... [\#37](https://github.com/uclouvain/openjpeg/issues/37)
- OpenJPEG-1.3.0 pclr, cmap and cdef [\#27](https://github.com/uclouvain/openjpeg/issues/27)
- realloc maybe too big \(t2.c\) [\#26](https://github.com/uclouvain/openjpeg/issues/26)
- libopenjpeg/opj\_malloc.h breaks on FreeBSD/Darwin
  systems [\#20](https://github.com/uclouvain/openjpeg/issues/20)
- image\_to\_j2k not outputting to win32 console
  properly [\#18](https://github.com/uclouvain/openjpeg/issues/18)
- \[OpenJPEG\] OpenJPEG\_v13: tiled image part
  2 [\#17](https://github.com/uclouvain/openjpeg/issues/17)
- JP2 Color Space modification by Matteo
  Italia [\#13](https://github.com/uclouvain/openjpeg/issues/13)
- Patch submission \( exotic video formats, and a few
  things \)  [\#12](https://github.com/uclouvain/openjpeg/issues/12)
- 16 bits lossy compression [\#10](https://github.com/uclouvain/openjpeg/issues/10)
- pnm file formats not accepting bitdepth greater than 8
  bpp [\#8](https://github.com/uclouvain/openjpeg/issues/8)
- Heap corruption in j2k encoder [\#5](https://github.com/uclouvain/openjpeg/issues/5)
- JPWL crash in marker reallocation\(+patch\), segfault while decoding image with main header
  protection [\#4](https://github.com/uclouvain/openjpeg/issues/4)
- a couple of small errors in libopenjpeg detected by
  coverity [\#1](https://github.com/uclouvain/openjpeg/issues/1)

**Closed issues:**

- Shared library build broken on ubuntu [\#728](https://github.com/uclouvain/openjpeg/issues/728)
- opj\_includes.h shouldn't
  define `\_\_attribute\_\_` [\#727](https://github.com/uclouvain/openjpeg/issues/727)
- Possible website problems due to Jekyll
  upgrade [\#713](https://github.com/uclouvain/openjpeg/issues/713)
- Stable Release? [\#712](https://github.com/uclouvain/openjpeg/issues/712)
- Meta Issue : try to fix some of these critical bugs before thinking about optimizing the
  library [\#710](https://github.com/uclouvain/openjpeg/issues/710)
- Tiled encoding broken for images with non power of 2
  dimensions [\#702](https://github.com/uclouvain/openjpeg/issues/702)
- install\_name \(still\) not set on OS X [\#700](https://github.com/uclouvain/openjpeg/issues/700)
- Add section in wiki describing where one can get test
  images [\#699](https://github.com/uclouvain/openjpeg/issues/699)
- Make EvenManager into singleton [\#698](https://github.com/uclouvain/openjpeg/issues/698)
- Remove old branches from repo [\#696](https://github.com/uclouvain/openjpeg/issues/696)
- MQ Coder encode: Conditional jump or move depends on uninitialised
  value\(s\) [\#695](https://github.com/uclouvain/openjpeg/issues/695)
- Can we add these files to our test
  suite ? [\#688](https://github.com/uclouvain/openjpeg/issues/688)
- -t and -d command line flags for decode are not documented on OpenJPEG
  website [\#685](https://github.com/uclouvain/openjpeg/issues/685)
- Decoding at the precinct level [\#676](https://github.com/uclouvain/openjpeg/issues/676)
- Support unscaled 10 bit data for 2K cinema @ 48 FPS, as per DCI
  standard [\#671](https://github.com/uclouvain/openjpeg/issues/671)
- Use parallel jobs in ctest [\#664](https://github.com/uclouvain/openjpeg/issues/664)
- \[Security\]Multiple Memory error [\#663](https://github.com/uclouvain/openjpeg/issues/663)
- lossy encoding a 16 bit TIF file : severe artifacts in decompressed
  image [\#660](https://github.com/uclouvain/openjpeg/issues/660)
- opj\_compress and opj\_decompress : get\_next\_file method uses hard-coded unix path
  separator [\#630](https://github.com/uclouvain/openjpeg/issues/630)
- Uninitialized variable [\#629](https://github.com/uclouvain/openjpeg/issues/629)
- Use of enum variable for bit flags prevents compilation as C++
  source [\#619](https://github.com/uclouvain/openjpeg/issues/619)
- Serious problem with quantization during lossy
  encoding [\#615](https://github.com/uclouvain/openjpeg/issues/615)
- Decompression does not work with sequential data
  source [\#613](https://github.com/uclouvain/openjpeg/issues/613)
- potential overflow in opj\_tcd\_tile\_t [\#605](https://github.com/uclouvain/openjpeg/issues/605)
- Logical condition [\#596](https://github.com/uclouvain/openjpeg/issues/596)
- file9.jp2 does not dump correctly on 1.5 [\#595](https://github.com/uclouvain/openjpeg/issues/595)
- opj\_compress man page is missing documentation of -jpip
  option [\#593](https://github.com/uclouvain/openjpeg/issues/593)
- opj\_compress fails to compress lossless on gcc/x86 \(-m32\) in 1.5
  branch [\#591](https://github.com/uclouvain/openjpeg/issues/591)
- Example: opj\_compress -i image.j2k -o
  image.pgm [\#577](https://github.com/uclouvain/openjpeg/issues/577)
- Mismatching delete [\#575](https://github.com/uclouvain/openjpeg/issues/575)
- Compilation fails on Win7 [\#546](https://github.com/uclouvain/openjpeg/issues/546)
- NR-JP2-file5.jp2-compare2base fails with third party
  libcms [\#540](https://github.com/uclouvain/openjpeg/issues/540)
- CTest spits out an error at the end of the test
  run [\#516](https://github.com/uclouvain/openjpeg/issues/516)
- opj\_uint\_adds\(\) is questionable [\#515](https://github.com/uclouvain/openjpeg/issues/515)
- Might consider renaming this method: [\#491](https://github.com/uclouvain/openjpeg/issues/491)
- opj\_compress run twice gives different fiile sizes for same
  file [\#490](https://github.com/uclouvain/openjpeg/issues/490)
- Android Support [\#483](https://github.com/uclouvain/openjpeg/issues/483)
- Add SSE2/SSE41 implementations for mct.c [\#451](https://github.com/uclouvain/openjpeg/issues/451)
- Reduce encoder code block memory usage for non 64x64 code block
  sizes [\#444](https://github.com/uclouvain/openjpeg/issues/444)
- valgrind "Uninitialized Memory Read" & "Uninitialized Memory Conditional"
  found  [\#438](https://github.com/uclouvain/openjpeg/issues/438)
- No way to debug opj\_tcd\_init\_encode\_tile or
  opj\_tcd\_init\_decode\_tile [\#433](https://github.com/uclouvain/openjpeg/issues/433)
- Add option to call dsymutil on built
  binaries [\#409](https://github.com/uclouvain/openjpeg/issues/409)
- Allow opj\_compress and opj\_decompress to read/write images over
  stdin/stdout [\#379](https://github.com/uclouvain/openjpeg/issues/379)
- reduce memory significantly for single tile RGB
  encoding [\#375](https://github.com/uclouvain/openjpeg/issues/375)
- Switch code repo to github and start using pull request
  workflow [\#373](https://github.com/uclouvain/openjpeg/issues/373)
- This is a BigTIFF file. This format not
  supported [\#125](https://github.com/uclouvain/openjpeg/issues/125)
- Add a test suite to check the convert
  functions [\#99](https://github.com/uclouvain/openjpeg/issues/99)
- Add build config to the dashboard to verify the autotools
  build [\#88](https://github.com/uclouvain/openjpeg/issues/88)

**Merged pull requests:**

- Correct abi-check.sh for
  PR [\#791](https://github.com/uclouvain/openjpeg/pull/791) ([mayeut](https://github.com/mayeut))
- Update
  tcd.c [\#790](https://github.com/uclouvain/openjpeg/pull/790) ([maddin200](https://github.com/maddin200))
- Update
  lcms2 [\#773](https://github.com/uclouvain/openjpeg/pull/773) ([mayeut](https://github.com/mayeut))
- Use lowercase for cmake commands
  consistently [\#769](https://github.com/uclouvain/openjpeg/pull/769) ([julienmalik](https://github.com/julienmalik))
- Ignore clang's summary
  warning [\#768](https://github.com/uclouvain/openjpeg/pull/768) ([julienmalik](https://github.com/julienmalik))
- Fix UBSan gcc warning for first arg to memset non
  null [\#767](https://github.com/uclouvain/openjpeg/pull/767) ([julienmalik](https://github.com/julienmalik))
- Update to
  libtiff-4.0.6 [\#764](https://github.com/uclouvain/openjpeg/pull/764) ([mayeut](https://github.com/mayeut))
- Fix
  warnings [\#763](https://github.com/uclouvain/openjpeg/pull/763) ([mayeut](https://github.com/mayeut))
- Check SSIZ is valid in
  opj\_j2k\_read\_siz [\#762](https://github.com/uclouvain/openjpeg/pull/762) ([mayeut](https://github.com/mayeut))
- Fix unsigned int overflow reported by
  UBSan [\#761](https://github.com/uclouvain/openjpeg/pull/761) ([mayeut](https://github.com/mayeut))
- Fix unsigned int overflow reported by
  UBSan [\#759](https://github.com/uclouvain/openjpeg/pull/759) ([mayeut](https://github.com/mayeut))
- Fix negative shift left reported by
  UBSan [\#758](https://github.com/uclouvain/openjpeg/pull/758) ([mayeut](https://github.com/mayeut))
- Fix negative shift left reported by
  UBSan [\#757](https://github.com/uclouvain/openjpeg/pull/757) ([mayeut](https://github.com/mayeut))
- Add clang 3.9 build to Travis
  matrix [\#753](https://github.com/uclouvain/openjpeg/pull/753) ([julienmalik](https://github.com/julienmalik))
- Fix implicit floating bool
  conversion [\#752](https://github.com/uclouvain/openjpeg/pull/752) ([julienmalik](https://github.com/julienmalik))
- Do not define \_\_attribute\_\_ in
  opj\_includes.h [\#751](https://github.com/uclouvain/openjpeg/pull/751) ([mayeut](https://github.com/mayeut))
- Allow to read/write 3/5/7/9/11/13/15 bpp TIF
  files [\#750](https://github.com/uclouvain/openjpeg/pull/750) ([mayeut](https://github.com/mayeut))
- Fix heap-buffer-overflow in
  color\_esycc\_to\_rgb [\#748](https://github.com/uclouvain/openjpeg/pull/748) ([mayeut](https://github.com/mayeut))
- update libpng to from 1.6.17 to
  1.6.21 [\#747](https://github.com/uclouvain/openjpeg/pull/747) ([julienmalik](https://github.com/julienmalik))
- Update cmake & jpylyzer for travis
  builds [\#746](https://github.com/uclouvain/openjpeg/pull/746) ([julienmalik](https://github.com/julienmalik))
- Fix Out-Of-Bounds Read in sycc42x\_to\_rgb
  function [\#745](https://github.com/uclouvain/openjpeg/pull/745) ([mayeut](https://github.com/mayeut))
- cppcheck fix for
  openjp2 [\#740](https://github.com/uclouvain/openjpeg/pull/740) ([julienmalik](https://github.com/julienmalik))
- Fix uninitialized variable reported by
  cppcheck [\#735](https://github.com/uclouvain/openjpeg/pull/735) ([julienmalik](https://github.com/julienmalik))
- Remove dead code in
  opj\_dump [\#734](https://github.com/uclouvain/openjpeg/pull/734) ([julienmalik](https://github.com/julienmalik))
- issue \#695 MQ Encode: ensure that bp pointer never points to uninitialized
  memory [\#708](https://github.com/uclouvain/openjpeg/pull/708) ([boxerab](https://github.com/boxerab))
- Fix issue
  135 [\#706](https://github.com/uclouvain/openjpeg/pull/706) ([mayeut](https://github.com/mayeut))
- Fix implementation of
  opj\_calloc [\#705](https://github.com/uclouvain/openjpeg/pull/705) ([stweil](https://github.com/stweil))
- \[git/2.1 regression\] Fix opj\_write\_tile\(\) failure when
  numresolutions=1 [\#690](https://github.com/uclouvain/openjpeg/pull/690) ([rouault](https://github.com/rouault))
- Fix fatal crash on 64 bit
  Linux [\#687](https://github.com/uclouvain/openjpeg/pull/687) ([stweil](https://github.com/stweil))
- \[libtiff\] Add missing include statement for
  ssize\_t [\#686](https://github.com/uclouvain/openjpeg/pull/686) ([mayeut](https://github.com/mayeut))
- Fix duplicate article in
  comments [\#684](https://github.com/uclouvain/openjpeg/pull/684) ([stweil](https://github.com/stweil))
- Fix grammar in
  comment [\#679](https://github.com/uclouvain/openjpeg/pull/679) ([stweil](https://github.com/stweil))
- Remove whitespace and CR at line
  endings [\#678](https://github.com/uclouvain/openjpeg/pull/678) ([stweil](https://github.com/stweil))
- Fix
  typos [\#665](https://github.com/uclouvain/openjpeg/pull/665) ([jwilk](https://github.com/jwilk))
- Add missing source for the JPIP library and executables \(issue
  \#658\) [\#659](https://github.com/uclouvain/openjpeg/pull/659) ([stweil](https://github.com/stweil))
- Fix undefined size jp2 box
  handling [\#654](https://github.com/uclouvain/openjpeg/pull/654) ([mayeut](https://github.com/mayeut))
- opj\_decompress: Update error
  message [\#651](https://github.com/uclouvain/openjpeg/pull/651) ([stweil](https://github.com/stweil))
- Fix support of posix\_memalloc for
  Linux [\#648](https://github.com/uclouvain/openjpeg/pull/648) ([stweil](https://github.com/stweil))
- Fix typo in
  comments [\#647](https://github.com/uclouvain/openjpeg/pull/647) ([stweil](https://github.com/stweil))
- Avoid pointer arithmetic with \(void \*\)
  pointers [\#644](https://github.com/uclouvain/openjpeg/pull/644) ([smuehlst](https://github.com/smuehlst))
- Fix HP compiler warning about redeclaration of function
  \(\#640\) [\#641](https://github.com/uclouvain/openjpeg/pull/641) ([smuehlst](https://github.com/smuehlst))
- Fix format strings and unneeded
  assignment [\#638](https://github.com/uclouvain/openjpeg/pull/638) ([stweil](https://github.com/stweil))
- Fix repository for JPEG2000 test
  data [\#637](https://github.com/uclouvain/openjpeg/pull/637) ([stweil](https://github.com/stweil))
- Update allocation
  functions [\#636](https://github.com/uclouvain/openjpeg/pull/636) ([mayeut](https://github.com/mayeut))
- Fix OpenJPEG GitHub issue
  \#633. [\#634](https://github.com/uclouvain/openjpeg/pull/634) ([smuehlst](https://github.com/smuehlst))
- travis-ci: Include add ons in
  matrix [\#632](https://github.com/uclouvain/openjpeg/pull/632) ([mayeut](https://github.com/mayeut))
- Add
  Appveyor [\#627](https://github.com/uclouvain/openjpeg/pull/627) ([mayeut](https://github.com/mayeut))
- Use Travis-ci to run ABI
  check [\#626](https://github.com/uclouvain/openjpeg/pull/626) ([mayeut](https://github.com/mayeut))
- Fix warnings for
  C++ [\#623](https://github.com/uclouvain/openjpeg/pull/623) ([stweil](https://github.com/stweil))
- Fixed problem that C++ compilation failed because of enum
  variable. [\#622](https://github.com/uclouvain/openjpeg/pull/622) ([smuehlst](https://github.com/smuehlst))
- Added missing casts for return values of
  opj\_malloc\(\)/opj\_calloc\(\). [\#618](https://github.com/uclouvain/openjpeg/pull/618) ([smuehlst](https://github.com/smuehlst))
- Add check for seek support before trying TPsot==TNsot
  workaround [\#617](https://github.com/uclouvain/openjpeg/pull/617) ([mayeut](https://github.com/mayeut))
- Fix some typos found by
  codespell [\#610](https://github.com/uclouvain/openjpeg/pull/610) ([stweil](https://github.com/stweil))
- Correct leak in
  color\_cielab\_to\_rgb [\#590](https://github.com/uclouvain/openjpeg/pull/590) ([mayeut](https://github.com/mayeut))
- Add Travis-ci build
  matrix [\#584](https://github.com/uclouvain/openjpeg/pull/584) ([mayeut](https://github.com/mayeut))
- Correct lossless issue on linux
  x86 [\#579](https://github.com/uclouvain/openjpeg/pull/579) ([mayeut](https://github.com/mayeut))
- Travis-ci
  update [\#578](https://github.com/uclouvain/openjpeg/pull/578) ([mayeut](https://github.com/mayeut))
- Correct CMake version
  requirements [\#572](https://github.com/uclouvain/openjpeg/pull/572) ([mayeut](https://github.com/mayeut))
- Add tests for
  CMYK/esYCC/CIELab [\#567](https://github.com/uclouvain/openjpeg/pull/567) ([mayeut](https://github.com/mayeut))
- Add support for CIELab, EYCC and
  CMYK [\#559](https://github.com/uclouvain/openjpeg/pull/559) ([szukw000](https://github.com/szukw000))
- Remove printf/fprintf to stdout/stderr throughout openjp2
  lib [\#558](https://github.com/uclouvain/openjpeg/pull/558) ([mayeut](https://github.com/mayeut))
- better -ffast-math
  handling [\#555](https://github.com/uclouvain/openjpeg/pull/555) ([rdieter](https://github.com/rdieter))
- Add jpylyzer tests for JP2
  compression [\#552](https://github.com/uclouvain/openjpeg/pull/552) ([mayeut](https://github.com/mayeut))
- Add COC/QCC in main header when
  needed [\#551](https://github.com/uclouvain/openjpeg/pull/551) ([mayeut](https://github.com/mayeut))
- Use \_\_emul under msvc x86 for fast 64 = 32 \*
  32 [\#550](https://github.com/uclouvain/openjpeg/pull/550) ([mayeut](https://github.com/mayeut))
- Update convert for PNG
  output [\#549](https://github.com/uclouvain/openjpeg/pull/549) ([mayeut](https://github.com/mayeut))
- Remove some warnings when
  building [\#548](https://github.com/uclouvain/openjpeg/pull/548) ([mayeut](https://github.com/mayeut))
- Switch to
  libpng-1.6.17 [\#547](https://github.com/uclouvain/openjpeg/pull/547) ([mayeut](https://github.com/mayeut))
- Add some missing static
  keywords [\#545](https://github.com/uclouvain/openjpeg/pull/545) ([mayeut](https://github.com/mayeut))
- Switch to libcms2
  mm2/Little-CMS@0e8234e090d6aab33f90e2eb0296f30aa0705e57 [\#544](https://github.com/uclouvain/openjpeg/pull/544) ([mayeut](https://github.com/mayeut))
- Prevent overflow when coding 16 bits
  images [\#543](https://github.com/uclouvain/openjpeg/pull/543) ([mayeut](https://github.com/mayeut))
- Switch to
  libcms2-2.6 [\#542](https://github.com/uclouvain/openjpeg/pull/542) ([mayeut](https://github.com/mayeut))
- Update PNG
  support [\#538](https://github.com/uclouvain/openjpeg/pull/538) ([mayeut](https://github.com/mayeut))
- Various Minor
  fixes [\#537](https://github.com/uclouvain/openjpeg/pull/537) ([mayeut](https://github.com/mayeut))
- Update TIFF conversion to support more bit
  depth. [\#535](https://github.com/uclouvain/openjpeg/pull/535) ([mayeut](https://github.com/mayeut))
- Add checks for odd looking cmap & for cmap outside jp2h
  box [\#534](https://github.com/uclouvain/openjpeg/pull/534) ([mayeut](https://github.com/mayeut))
- Refactor opj\_j2k\_read\_ppm &
  opj\_j2k\_read\_ppt [\#533](https://github.com/uclouvain/openjpeg/pull/533) ([mayeut](https://github.com/mayeut))
- Add option to force component splitting in
  imagetopnm [\#531](https://github.com/uclouvain/openjpeg/pull/531) ([mayeut](https://github.com/mayeut))
- fix Suspicious code in j2k.c
  \#517 [\#529](https://github.com/uclouvain/openjpeg/pull/529) ([renevanderark](https://github.com/renevanderark))
- Update zlib to version
  1.2.8 [\#528](https://github.com/uclouvain/openjpeg/pull/528) ([mayeut](https://github.com/mayeut))
- Fix opj\_write\_bytes\_BE
  \(\#518\) [\#521](https://github.com/uclouvain/openjpeg/pull/521) ([manisandro](https://github.com/manisandro))
- Correctly decode files with incorrect tile-part header fields
  \(TPsot==TNsot\) [\#514](https://github.com/uclouvain/openjpeg/pull/514) ([mayeut](https://github.com/mayeut))
- Fixed
  typos [\#510](https://github.com/uclouvain/openjpeg/pull/510) ([radarhere](https://github.com/radarhere))
- Formatted the readme
  file [\#507](https://github.com/uclouvain/openjpeg/pull/507) ([htmfilho](https://github.com/htmfilho))

## [version.2.1](https://github.com/uclouvain/openjpeg/releases/tag/version.2.1) (2014-04-29)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.2.0.1...version.2.1)

## [version.2.0.1](https://github.com/uclouvain/openjpeg/releases/tag/version.2.0.1) (2014-04-22)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.5.2...version.2.0.1)

## [version.1.5.2](https://github.com/uclouvain/openjpeg/releases/tag/version.1.5.2) (2014-03-28)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.2.0...version.1.5.2)

## [version.2.0](https://github.com/uclouvain/openjpeg/releases/tag/version.2.0) (2014-03-28)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.5.1...version.2.0)

## [version.1.5.1](https://github.com/uclouvain/openjpeg/releases/tag/version.1.5.1) (2012-09-13)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.5...version.1.5.1)

## [version.1.5](https://github.com/uclouvain/openjpeg/releases/tag/version.1.5) (2012-02-07)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.3...version.1.5)

## [version.1.3](https://github.com/uclouvain/openjpeg/releases/tag/version.1.3) (2011-07-03)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.4...version.1.3)

## [version.1.4](https://github.com/uclouvain/openjpeg/releases/tag/version.1.4) (2011-07-03)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.2...version.1.4)

## [version.1.2](https://github.com/uclouvain/openjpeg/releases/tag/version.1.2) (2007-06-04)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.1...version.1.2)

## [version.1.1](https://github.com/uclouvain/openjpeg/releases/tag/version.1.1) (2007-01-31)

List of fixed issues and enhancements unavailable,
see [NEWS](https://github.com/uclouvain/openjpeg/blob/master/NEWS.md)
or [Full Changelog](https://github.com/uclouvain/openjpeg/compare/version.1.0...version.1.1)

\* *This Change Log was automatically generated
by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*