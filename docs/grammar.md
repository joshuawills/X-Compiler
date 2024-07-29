# Grammar for 'X'

$$
\begin{align}
\textit{program} &\to (\textit{function }|\textit{ global-var})^*  \\ \\
\textit{global-var} &\to \textit{decl } (\text{ "=" }\textit{ expr})? (\textit{","} \textit{ ident} (\textit{[]})?  (\text{ "=" } \textit{ expr}?))^* \text{ ";"}\\
\textit{local-var} &\to \textit{decl } (\text{ "=" }\textit{ expr})? (\textit{","} \textit{ ident} (\textit{[]})? (\text{ "=" } \textit{ expr}?))^* \text{ ";"}\\
\textit{function} &\to \textbf{fn} \textit{ ident} \text{ "\\("} \textit{ para-list} \text{ "\\)"} \text{ "->"}
\textit{type} \textit{ compound-stmt}\\ \\

\text{compound-stmt} &\to \text{"\\\{" } (\textit{stmt } | \textit{ local-var})^* \text{ "\\\}"} \\
\text{stmt} &\to
\begin{cases}
\textit{compound-stmt} \\
\textit{if-stmt} \\
\textit{for-stmt} \\
\textit{while-stmt} \\
\textit{do-while-stmt} \\
\textit{break-stmt} \\
\textit{continue-stmt} \\
\textit{return-stmt} \\
\textit{decl-stmt} \\
\textit{math-decl-stmt} \\
\textit{func-call-stmt} \\
\textit{loop-stmt} \\
\end{cases} \\
\textit{if-stmt} &\to \textbf{if} \textit{ expr stmt } (\textbf{else if } \textit{expr stmt})^* (\textbf{else} \textit{
stmt})? \\
\textit{for-stmt} &\to \textbf{for} \textit{ expr?} \text{ ";"} \textit{ expr?} \text{ ";"} \textit{ expr? stmt} \\
\textit{while-stmt} &\to \textbf{while} \textit{ expr stmt} \\
\textit{do-while-stmt} &\to \textbf{do} \textit{ compound-stmt expr} \\
\textit{break-stmt} &\to \textbf{break} \text{ ";"} \\
\textit{continue-stmt} &\to \textbf{continue } \text{ ";"} \\
\textit{return-stmt} &\to \textbf{return} \textit{ expr}? \text{ ";"} \\
\textit{math-decl-stmt} &\to \textit(*)? \textit{ ident} (\text{ "+="} | \text{ "-="} | \text{ "/="} | \text{ "*="}) \textit{expr} \text{ ";"} \\
\textit{decl-stmt} &\to \textit(*)? \textit{ ident} \text{ "="} \textit{expr} \text{ ";"} \\
\textit{func-call-stmt} &\to \textit{func-call} \text{ ";"} \\
\textit{loop-stmt} &\to \textbf{ loop} ( \textit{ident } \textbf{in})? \textit{ INTLITERAL?} \textit{ INTLITERAL?} \textit{ compound-stmt}\\
\textit{loop-stmt} &\to \textbf{ loop} \textit{ ident } \textit{ compound-stmt}\\ \\

\textit{expr} &\to \textit{or-expr}\\
\textit{or-expr} &\to \textit{and-expr } (\text{"||" } \textit{and-expr})^* \\
\textit{and-expr} &\to \textit{equality-expr } (\text{"\\\&\\\&" } \textit{equality-expr})^* \\
\textit{equality-expr} &\to \textit{relational-expr } ((\text{"==" } | \text{ "!=" }) \textit{ relational-expr})^* \\
\textit{relational-expr} &\to \textit{additive-expr } ((\text{"<" } | \text{ "<=" } | \text{ ">" } | \text{ ">=" })
\textit{ additive-expr})^* \\
\textit{additive-expr} &\to \textit{mult-expr } ((\text{"-" } | \text{ "+" }) \textit{ mult-expr})^* \\
\textit{mult-expr} &\to \textit{unary-expr } ((\text{"\%" } | \text{"*" } | \text{ "/" }) \textit{ unary-expr})^* \\
\textit{unary-expr} &\to
\begin{cases}
\textbf{INTLITERAL} \\
\textbf{FLOATLITERAL} \\
\textbf{STRINGLITERAL} \\
\textbf{BOOLLITERAL} \\
"(" \textit{ expr } ")" \\
\textit{ident} \\
\textit{(+ | - | ! | * | \&) unary-expr} \\
\textit{func-call} \\
\textit{array-init-expr} \\
\textit{array-index} \\
\end{cases} \\

\\

\textit{array-index} &\to \textit{ident } "[" \textit{expr} "]" \\
\textit{func-call} &\to \textit{ident } "(" \textit{args} ")" \\
\textit{array-init-expr} &\to \textit{"["} (\textit{expr } (\text{","} \textit{ expr})^*)? \textit{"]"} \\
\textit{args} &\to \textit{expr } (\text{","} \textit{ expr})^* \text{ | } \epsilon\\
\textit{para-list} &\to \textit{decl } (\textit{"," decl})^*  \text{ | } \epsilon \\ \\
\textit{decl} &\to \textbf{mut}? \textit{ type ident}  (\textit{[]})?\\

\textit{ident} &\to \textbf{letter} (\textbf{letter } | \textbf{ digit})^* || \textit{ \$} \\
\textit{type} &\to \textbf{int | str | void | bool | float | \textit{type}* } \\

\textit{INTLITERAL} &\to [0-9]+ \\
\textit{FLOATLITERAL} &\to [0-9]^+\textit{"."}[0-9]? \\
\textit{STRINGLITERAL} &\to \textit{"} \textit{ident} \textit{"} \\
\textit{BOOLLITERAL} &\to true | false

\end{align}
$$