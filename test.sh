# !/bin/bash
java -cp assign1.jar:assign1-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.assign1.LanguageModelTester -path ./assign1_data -lmType $1 -noprint
