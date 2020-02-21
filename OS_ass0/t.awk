BEGIN {FS = "."}
{print "\\textit{", $1, ".", $2, "}\\newline"}
{print "\\textbf{Answer: ...}\\\\ \\\\"}