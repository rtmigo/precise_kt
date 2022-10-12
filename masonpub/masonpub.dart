#!/usr/bin/env dart

import 'dart:io';

void createDocs() {
  final r = Process.runSync("./gradlew", ["clean", "dokkaHtml"]);
  if (r.exitCode!=0)
    throw "Bad exit code";


}

void main() {
  createDocs();
}