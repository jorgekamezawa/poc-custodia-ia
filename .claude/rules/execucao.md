# Ao executar comandos e editar arquivos

Vale para qualquer pasta. Nasceu dos lapsos do mandato 1 (`docs/decisoes/0001-mandato-1.md`); o que uma ferramenta consegue barrar já é barrado por hook ou script, e aqui fica o resto.

## Editar
- Edite com a ferramenta Edit: ela falha quando o texto não casa. Substituição por script (Python, `sed`) só com conferência explícita de que casou (`assert` ou `grep` depois), porque o formatador muda o texto entre uma leitura e outra.
- Depois de editar um arquivo formatado (Java, TypeScript, HTML), confira a mudança no arquivo, não no que você mandou escrever.
- Texto com crase ou barra invertida vai por arquivo ou heredoc com aspas simples (`<<'EOF'`), nunca escapado dentro de outra linguagem.

## Encadear comandos
- Cadeia em que um passo depende do anterior começa com `set -eo pipefail`. No zsh, erro dentro de heredoc nem sempre interrompe a cadeia: confira o código de saída do passo que importa.
- Nunca `| tail` ou `| grep` num passo cujo código de saída decide o próximo: o pipe devolve o código do último comando.
- Teste vermelho interrompe a cadeia antes de commit, push ou PR.

## Processos
- Pare processo pela porta (runbook 0001), nunca por padrão de nome: `pkill -f` é negado pelo hook.
- Merge só por `.claude/skills/abrir-pr/scripts/merge_when_green.py`: `gh pr merge` direto é negado pelo hook.
