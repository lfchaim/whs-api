--10/05/2022
CREATE TABLE "query_builder" (
  "id" SERIAL PRIMARY KEY NOT NULL,
  "key" varchar(10) UNIQUE,
  "value" text
);
alter table query_builder add comment varchar(50) null;
--12/12/2022
CREATE TABLE public.query_builder_sub (
	id serial4 NOT NULL,
	id_query_builder int4 NOT NULL,
	"alias_name" varchar NULL,
	value text NULL,
	"comment" varchar NULL,
	CONSTRAINT query_builder_sub_pkey PRIMARY KEY (id)
);
ALTER TABLE "query_builder_sub" ADD CONSTRAINT "query_builder_sub_fk1" FOREIGN KEY ("id_query_builder") REFERENCES "query_builder" ("id");
ALTER TABLE public.query_builder ADD has_sub boolean default false;
--31/05/2022
CREATE TABLE public.query_param (
	id serial4 NOT NULL,
	"key" varchar(50) NULL,
	value text NULL,
	"comment" varchar(50) NULL,
	CONSTRAINT query_param_uk1 UNIQUE (key),
	CONSTRAINT query_param_pkey PRIMARY KEY (id)
);
--20/12/2022
ALTER TABLE public.query_param ADD exclude_fields VARCHAR NULL;