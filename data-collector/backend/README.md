# code-with-quarkus Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


## Path Table

| Method | Path | Description |
| --- | --- | --- |
| GET | [/tables/rows/get5UnansweredRows](#gettablesrowsget5unansweredrows) |  |
| POST | [/tables/rows/submit5UnansweredRows](#posttablesrowssubmit5unansweredrows) |  |

## Reference Table

| Name | Path | Description |
| --- | --- | --- |
| TableCellDTO | [#/components/schemas/TableCellDTO](#componentsschemastablecelldto) |  |
| TableRowDTO | [#/components/schemas/TableRowDTO](#componentsschemastablerowdto) |  |

## Path Details

***

### [GET]/tables/rows/get5UnansweredRows

#### Responses

- 200 OK

`application/json`

```ts
{
  tableId?: string
  rowEntries: {
    cellValue?: string
    rowName?: string
    columnId?: integer
  }[]
  question?: string
  row?: integer
  answerColumn?: integer
  content?: string
}[]
```

***

### [POST]/tables/rows/submit5UnansweredRows

#### RequestBody

- application/json

```ts
{
  tableId?: string
  rowEntries: {
    cellValue?: string
    rowName?: string
    columnId?: integer
  }[]
  question?: string
  row?: integer
  answerColumn?: integer
  content?: string
}[]
```

#### Responses

- 201 Created

## References

### #/components/schemas/TableCellDTO

```ts
{
  cellValue?: string
  rowName?: string
  columnId?: integer
}
```

### #/components/schemas/TableRowDTO

```ts
{
  tableId?: string
  rowEntries: {
    cellValue?: string
    rowName?: string
    columnId?: integer
  }[]
  question?: string
  row?: integer
  answerColumn?: integer
  content?: string
}
```
